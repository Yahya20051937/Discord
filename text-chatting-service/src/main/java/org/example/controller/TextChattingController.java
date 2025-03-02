package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.MediaType;
import org.example.dto.SendMessageRequest;
import org.example.dto.UploadMediaChunkRequest;
import org.example.service.ChattingService;
import org.example.service.MediaService;
import org.hibernate.engine.jdbc.env.internal.DefaultSchemaNameResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class TextChattingController {
    @Autowired
    ChattingService chattingService;

    @Autowired
    MediaService mediaService;

    @GetMapping("/api/text-chatting/auth/getNMessages/beforeX")
    public ResponseEntity<?> getLastNRoomMessagesBeforeX(@RequestParam String roomId, @RequestParam int n, @RequestParam int x, HttpServletRequest request){
        return chattingService.getLastNRoomMessagesBeforeX(n, x, roomId, request.getHeader("Authorization"));
    }

    @PostMapping("/api/text-chatting/auth/write")
    public ResponseEntity<?> write(@RequestBody SendMessageRequest sendMessageRequest, HttpServletRequest request){
        return chattingService.sendMessage(sendMessageRequest, request.getHeader("Authorization"));

    }


    @PostMapping("/api/text-chatting/upload/message-media")
    public ResponseEntity<?> uploadMessageMedia(@RequestBody UploadMediaChunkRequest uploadMediaChunkRequest, HttpServletRequest request){
        return ResponseEntity.status(mediaService.uploadMessageMediaChunk(uploadMediaChunkRequest, request.getHeader("uploadToken")))
                .build();
    }

    @GetMapping("/api/text-chatting/auth/download/message-image")
    public ResponseEntity<?> downloadMessageImage(@RequestParam("id") String id,  HttpServletRequest request){
        return mediaService.downloadMessageImage(id, request.getHeader("Authorization"));

    }

    @GetMapping(value = "/api/text-chatting/auth/stream/message-video", produces = "video/mp4")
    public Mono<Resource> streamVideo(@RequestParam("id") String id, HttpServletRequest request){
        return mediaService.streamMessageMedia(id, request.getHeader("Authorization"), MediaType.VIDEO);
    }

    @GetMapping(value = "/api/text-chatting/auth/stream/message-audio", produces = "video/mp3")
    public Mono<Resource> streamAudio(@RequestParam("id") String id, HttpServletRequest request){
        return mediaService.streamMessageMedia(id, request.getHeader("Authorization"), MediaType.AUDIO);
    }

    @GetMapping("/api/text-chatting/get/room/messages-count")
    public Long getRoomMessagesCount(@RequestParam String roomId){
        return chattingService.getRoomMessagesCount(roomId);
    }

    @PutMapping("/api/text-chatting/auth/handle/upload-failure")
    public void handleUploadFailure(@RequestParam String id, HttpServletRequest request){
        chattingService.handleUploadFailure(id, request.getHeader("Authorization"));
    }


}
