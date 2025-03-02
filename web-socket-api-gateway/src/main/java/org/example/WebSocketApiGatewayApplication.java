package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class WebSocketApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebSocketApiGatewayApplication.class, args);
    }
}