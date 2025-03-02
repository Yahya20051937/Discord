package org.example.config;

import jakarta.annotation.PreDestroy;
import org.example.service.EndpointsService;
import org.example.service.RoomConnectionService;
import org.example.service.UserConnectionService;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class PreShutdownHook implements SmartLifecycle {
    private boolean running = true;

    @Autowired
    EndpointsService endpointsService;

    @Autowired
    RoomConnectionService roomConnectionService;

    @Override
    public void stop(Runnable callback) {
        roomConnectionService.handleDisconnectionToAll();
        endpointsService.deleteEndpoint();
        callback.run();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        System.out.println("stop() method called...");
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE; // Ensures it runs before other lifecycle beans
    }
}

