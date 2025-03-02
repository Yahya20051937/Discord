package org.example.config;


import jakarta.servlet.ServletContext;
import org.example.websocket.AuthHandshakeInterceptor;
import org.example.websocket.WebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.*;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    @Autowired
    AuthHandshakeInterceptor interceptor;

    @Autowired
    WebSocketHandler webSocketHandler;


    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/room-connection/ws/**")
                .addInterceptors(interceptor)
                ;
    }

    @Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(1024 * 1024); // reduce later....
        container.setMaxBinaryMessageBufferSize(1024 * 1024); // reduce later...

        return container;
    }

    @Bean
    public WebSocketTransportRegistration webSocketTransportRegistration() {
        WebSocketTransportRegistration registration = new WebSocketTransportRegistration();
        registration.setMessageSizeLimit(1024 * 1024);

        return registration;
    }




}
