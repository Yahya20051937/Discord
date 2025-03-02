package org.example.service;

import org.example.AtomicResponse;
import org.example.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
public class MediaService {
    @Autowired
    DecodingService decodingService;

    @Autowired
    UserService userService;

    public void createEmptyFile(String username){
        try {
            Files.createFile(Path.of("/discord-users-images/" + username + ".jpeg"));
        } catch (IOException e){
            e.printStackTrace();
        }

    }


    public HttpStatus uploadUserImage(String bytes64, String token) throws ParseException {
        User user = decodingService.getUserFromJwt(token);
        if (user != null){
                byte[] mediaChunkAsBytes = Base64.getDecoder().decode(bytes64);
                try {
                    Files.write(
                            Path.of("/discord-users-images/" + user.getUsername() + ".jpeg"),
                            mediaChunkAsBytes,
                            StandardOpenOption.APPEND
                    );
                    userService.editHasImageAttribute(user.getUsername(), true);
                    return HttpStatus.OK;
                } catch (Exception e){
                    e.printStackTrace();
                    return HttpStatus.INTERNAL_SERVER_ERROR;
                }

            }

        return HttpStatus.NOT_FOUND;

    }

    public ResponseEntity<?> downloadUserImage(String username) throws ParseException {
        if (userService.doesUserHaveImage(username)){
            try {
                byte[] bytes = Files.readAllBytes(Path.of("/discord-users-images/" + username + ".jpeg"));
                return ResponseEntity
                        .status(200)
                        .body(Map.of("bytes64", Base64.getEncoder().encodeToString(bytes)));

            } catch (IOException e){
                e.printStackTrace();
            }
        }
        return ResponseEntity
                .status(404)
                .build();
    }



















}
