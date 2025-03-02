package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.JoinQuit;
import org.example.config.RabbitConfig;
import org.example.dto.response.ServerDto;
import org.example.entitiy.MemberShip;
import org.example.entitiy.Server;
import org.example.repository.MemberShipRepository;
import org.example.repository.ServerRepository;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MemberShipService {
    @Autowired
    public MemberShipRepository memberShipRepository;

    @Autowired
    ServerRepository serverRepository;

    @Autowired
    UserService userService;

    @Autowired
    RabbitConfig rabbitConfig;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ServerNotificationService serverNotificationService;

    public List<ServerDto> getJoinedServers(String token){
        String username = userService.getUser(token)
                .getUsername();
        return memberShipRepository.findByMember(username)
                .stream()
                .map(it -> serverRepository.findById(it.getServer().getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ServerDto::new)
                .toList();
    }
    public HttpStatusCode joinOrQuit(String serverId, String token, JoinQuit joinQuit){
        String username = userService.getUser(token).getUsername();
        AtomicBoolean isServerFound = new AtomicBoolean(false);
        AtomicBoolean isConflict = new AtomicBoolean(false);
        serverRepository.findById(serverId)
                .ifPresent(
                        it -> {
                            isServerFound.set(true);
                            memberShipRepository.findByMemberAndServerId(username, serverId).ifPresentOrElse(
                                    m -> {
                                        if (joinQuit.equals(JoinQuit.JOIN))
                                            isConflict.set(true);
                                        else if (joinQuit.equals(JoinQuit.QUIT)) {
                                            memberShipRepository.delete(m);
                                            serverNotificationService.notifyServerQuit(username, serverId);
                                        }
                                        },
                                    () -> {
                                        if (joinQuit.equals(JoinQuit.JOIN)) {
                                            memberShipRepository.save(new MemberShip(username, it));
                                            serverNotificationService.notifyServerJoin(username, serverId);
                                            this.sendMessageToAssignMemberRoleToNewMember(serverId, username);
                                        }
                                        else if (joinQuit.equals(JoinQuit.QUIT))
                                            isConflict.set(true);

                                    }
                            );
                        }
                );
        if (isServerFound.get())
            if (isConflict.get())
                return HttpStatus.CONFLICT;
            else
                return HttpStatus.OK;
        else
            return HttpStatus.NOT_FOUND;
    }

    public Boolean isUserMemberOfServer(String serverId, String username){
        return memberShipRepository.findByMemberAndServerId(username, serverId).isPresent();
    }

    private void sendMessageToAssignMemberRoleToNewMember(String serverId, String username){
        try {
            Map<String, String> messageBody = Map.of(
                    "Authorization", rabbitConfig.messagingToken(),
                    "action","assign-member-role",
                    "server-id", serverId,
                    "username", username
            );
            rabbitTemplate.send(
                    RabbitConfig.SERVER_ROLE_EXCHANGE,
                    RabbitConfig.SERVER_ROLE_ROUTING_KEY,
                    new Message(objectMapper.writeValueAsString(messageBody).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e){
            e.printStackTrace();
        }
    }


}
