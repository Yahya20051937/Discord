package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@SpringBootApplication
@EnableDiscoveryClient
@EnableWebSocket
public class TextChattingServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TextChattingServiceApplication.class, args);
    }
}