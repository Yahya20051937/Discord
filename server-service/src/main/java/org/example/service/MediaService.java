package org.example.service;

import org.example.dto.request.CreateRequest;
import org.example.entitiy.Server;
import org.example.repository.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;

@Service
public class MediaService {
    @Autowired
    ServerRepository serverRepository;


    public Boolean saveImage(CreateRequest createRequest, String serverId){
        try{
            Files.write(Path.of("/discord-servers-images/" + serverId + ".jpeg"), Base64.getDecoder().decode(createRequest.getLogoBytes64()),  StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, String> getServerImage(String serverId){
        Optional<Server> server = serverRepository.findById(serverId);
        if (server.isPresent())
            try {
                byte[] bytes =  Files.readAllBytes(Path.of("/discord-servers-images/" + server.get().getId() + ".jpeg"));
                return Map.of("bytes64", Base64.getEncoder().encodeToString(bytes));
            } catch (Exception e){
                e.printStackTrace();
            }
        return null;
    }

    public void deleteImage(String serverId){
        try {
            Files.deleteIfExists(Path.of("/discord-servers-images/" + serverId+ ".jpeg"));
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
