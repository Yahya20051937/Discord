package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.dto.AuthorizationRequest;
import org.example.dto.RegistrationRequest;
import org.example.model.User;
import org.example.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Map;

@RestController
public class UserServiceController {
    @Autowired
    RegistrationService registrationService;
    @Autowired
    AuthorizationService authorizationService;
    @Autowired
    DecodingService decodingService;

    @Autowired
    MediaService mediaService;


    @PostMapping("/api/user/register")
    public ResponseEntity<?> register(@RequestBody RegistrationRequest request){
        return registrationService.register(request);
    }

    @PostMapping("/api/user/auth/upload-image")
    public ResponseEntity<?> uploadUserImage(@RequestBody Map<String, String> requestBody, HttpServletRequest request) throws ParseException {
        return ResponseEntity.status(
                mediaService.uploadUserImage(requestBody.get("bytes64"), request.getHeader("Authorization"))
        ).build();
    }

    @GetMapping("/api/user/download-image")
    public ResponseEntity<?> downloadUserImage(@RequestParam("username") String username) throws ParseException {
        return mediaService.downloadUserImage(username);
    }

    @PostMapping("/api/user/authorize")
    public ResponseEntity<?> authorize(@RequestBody AuthorizationRequest request){
        return authorizationService.authorize(request);
    }

    @PostMapping("/api/user/token/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> requestBody){
        return authorizationService.refresh(requestBody.get("refreshToken"));
    }

    @GetMapping("/api/user/get")
    public User getUser(HttpServletRequest request) throws ParseException {
        try {
            return decodingService.getUserFromJwt(request.getHeader("Authorization"));
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/api/user/message-broker/authorized")
    public Boolean isMessageBrokerAuthorized(HttpServletRequest request){
        return decodingService.isMessageBrokerAuthorized(request.getHeader("token"));
    }


}
