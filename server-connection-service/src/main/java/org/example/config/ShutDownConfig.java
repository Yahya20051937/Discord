package org.example.config;

import jakarta.annotation.PreDestroy;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ShutDownConfig {
    @Autowired
    RabbitAdmin rabbitAdmin;
    @Autowired
    RabbitConfig config;

    @PreDestroy
    private void onShutDown(){
        this.rabbitAdmin.deleteQueue("server-connections/queue/" + config.queueId);
        this.rabbitAdmin.deleteExchange("server-connections/exchange/" + config.queueId);
    }
}
