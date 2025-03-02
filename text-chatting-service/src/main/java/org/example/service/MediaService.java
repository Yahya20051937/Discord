package org.example.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.UrlResource;
import org.example.AtomicResponse;
import org.example.MediaType;
import org.example.ScopeType;
import org.example.dto.UploadMediaChunkRequest;
import org.example.entity.Message;
import org.example.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.nio.file.Paths;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

@Service
public class MediaService {
    @Autowired
    UserService userService;

    @Autowired
    RoomScopeService roomScopeService;

    @Autowired
    MessageRepository messageRepository;
    private String getExtension(MediaType mediaType){
        return switch (mediaType){
            case IMAGE -> ".jpeg";
            case VIDEO -> ".mp4";
            case AUDIO -> ".mp3";
            case NONE -> "."     ;
        };
    }

    public void createEmptyFile(String messageId, MediaType mediaType){
        try {
            Files.createFile(Path.of("/discord-messages-media/" + messageId + this.getExtension(mediaType)));
        } catch (IOException e){
            e.printStackTrace();
        }

    }
    public HttpStatus uploadMessageMediaChunk(UploadMediaChunkRequest request, String uploadToken){
        if (userService.isClientAuthorizedToUpload(uploadToken)){
           Optional<Message> message = messageRepository.findById(request.getMessageId());
           if (message.isPresent()){
               byte[] mediaChunkAsBytes = Base64.getDecoder().decode(request.getMediaChunk64());
               try {
                   Files.write(
                           Path.of("/discord-messages-media/" + request.getMessageId() + this.getExtension(message.get().getMediaType())),
                           mediaChunkAsBytes,
                           StandardOpenOption.APPEND
                   );
                   return HttpStatus.OK;
               } catch (Exception e){
                   e.printStackTrace();
                   return HttpStatus.INTERNAL_SERVER_ERROR;
               }

           }

        }
        return HttpStatus.NOT_FOUND;

    }

    public ResponseEntity<?> downloadMessageImage(String messageId, String token){
        AtomicResponse atomicResponse = new AtomicResponse();
        messageRepository.findById(messageId)
                .ifPresentOrElse(
                        it -> {
                            if (!it.getMediaType().equals(MediaType.IMAGE))
                               atomicResponse.setStatus(400);
                            else
                                if (!roomScopeService.doesUserScope(it.getRoomId(), ScopeType.READ, token))
                                    atomicResponse.setStatus(409);
                                else
                                    try {
                                        byte[] bytes = Files.readAllBytes(Path.of("/discord-messages-media/" + it.getId() + ".jpeg"));
                                        atomicResponse.setBody(Map.of("bytes64", Base64.getEncoder().encodeToString(bytes)));
                                        atomicResponse.setStatus(200);
                                    } catch (IOException e){
                                        e.printStackTrace();
                                    }




                        } ,
                        () -> atomicResponse.setStatus(404)
                );
        return atomicResponse.toResponse();
    }

    public Mono<Resource> streamMessageMedia(String messageId, String token, MediaType mediaType){
        Optional<Message> message = messageRepository.findById(messageId);
        if (message.isPresent() && (message.get().getMediaType().equals(mediaType) ))
            if (roomScopeService.doesUserScope(message.get().getRoomId(), ScopeType.READ, token))
                try {
                    Resource resource = new UrlResource(Paths.get("/discord-messages-media/" + messageId + this.getExtension(mediaType)).toUri());
                    return Mono.fromSupplier(
                            () -> resource
                    );
                } catch (MalformedURLException e){
                    e.printStackTrace();
                }
        return Mono.empty();
    }

    public void clearMediaFile(String msgId, MediaType mediaType) throws IOException {
        Path path = Path.of("/discord-messages-media/" + msgId + this.getExtension(mediaType));
        Files.deleteIfExists(path);
        this.createEmptyFile(msgId, mediaType);
    }















}
