package org.example;

import jakarta.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableDiscoveryClient
@EnableWebSocket
public class RoomConnectionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoomConnectionServiceApplication.class, args);
    }


}