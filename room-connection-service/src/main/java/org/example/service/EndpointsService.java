package org.example.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.example.config.RabbitConfig;
import org.example.config.RedisConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

@Service
public class EndpointsService {
    private static final String KEY = "room-connection-services-endpoints";
    private String serverEndpoint = "host:0";

    @Autowired
    StringRedisTemplate stringRedisTemplate;
    public List<String> getEndpoints(){
        return stringRedisTemplate.opsForList().range(KEY, 0, -1);
    }



    @EventListener
    public void onApplicationEvent(final ServletWebServerInitializedEvent event) throws IOException {
        String port = String.valueOf(event.getWebServer().getPort());
        String containerId = new String(Files.readAllBytes(Path.of("/etc/hostname"))).trim();
        String endpoint = containerId + ":" + port;
        this.serverEndpoint = endpoint;
        System.out.println("Adding " + port + " to " + this.getEndpoints());
        stringRedisTemplate.opsForList().leftPush(KEY,  endpoint);
    }



    public void deleteEndpoint() {
        System.out.println("Adding " + this.serverEndpoint + "from " + this.getEndpoints());
        stringRedisTemplate.opsForList().remove(KEY, 0, serverEndpoint);
    }

}
