package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PreDestroy;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;

@Configuration
public class RabbitConfig {
    public final String queueId = UUID.randomUUID().toString();

    @Value("${private-key}")
    private String privateKey64;

    @Autowired
    WebClientConfig webClientConfig;



    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost("rabbitmq-discord");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("yahya");
        connectionFactory.setPassword("Wydad3719");
        connectionFactory.setVirtualHost("/");
        return connectionFactory;
    }

    @Bean
    public Queue queue(){
        return new Queue("room-connections/queue/" + this.queueId, false);
    }

    @Bean
    DirectExchange directExchange(){
        return new DirectExchange("room-connections/exchange/" + this.queueId);
    }


    @Bean
    Binding binding(){
        return BindingBuilder.bind(queue())
                .to(directExchange())
                .with("room-connections/routing-key/" + this.queueId);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.declareQueue(this.queue());
        admin.declareExchange(this.directExchange());
        admin.declareBinding(this.binding());
        return admin;
    }

    public String messagingToken() throws NoSuchAlgorithmException, InvalidKeySpecException {
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(
                new PKCS8EncodedKeySpec(
                        Base64.getDecoder().decode(privateKey64)
                )
        );

        return "Bearer " + Jwts.builder()
                .setSubject("secure-messaging")
                .setIssuer("room-connection-service")
                .setIssuedAt(Date.from(Instant.now()))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }


    public List<String> getAllQueues(BiFunction<String, String, Boolean> filter1, String...args){
        try {
            List<Map<String, Object>> maps =  webClientConfig.externalClient().get()
                    .uri("http://rabbitmq-discord:15672/api/queues")
                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("yahya:Wydad3719".getBytes()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
            assert maps != null;
            return  maps.stream()
                    .map(it -> (String) it.get("name"))
                    .filter(it -> {
                        try {
                            return filter1.apply(it, args[0]);
                        } catch (IndexOutOfBoundsException  e){
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .toList();
        } catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<String> getAllRoomConnectionsQueues(){
        return this.getAllQueues((String name, String key) -> name.split("/")[0].equals(key), "room-connections");
    }

    public String getRandomQueueId(){
        for (String queueName : this.getAllRoomConnectionsQueues()){
            String id = queueName.split("/")[2];
            if (!id.equals(this.queueId))
                return id;
        }
        return null;
    }




    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

}
