package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Configuration
public class RabbitConfig {
    public final String queueId = UUID.randomUUID().toString();

    @Value("${private-key}")
    private String privateKey64;

    @Bean
    public ConnectionFactory connectionFactory(){
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost("rabbitmq-discord");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("yahya");
        connectionFactory.setPassword("Wydad3719");
        connectionFactory.setVirtualHost("/");
        return connectionFactory;
    }

    @Bean
    public Queue queue(){
        return new Queue("server-connections/queue/" + this.queueId, false);
    }

    @Bean
    DirectExchange directExchange(){
        return new DirectExchange("server-connections/exchange/" + this.queueId);
    }


    @Bean
    Binding binding(){
        return BindingBuilder.bind(queue())
                .to(directExchange())
                .with("server-connections/routing-key/" + this.queueId);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);
        admin.declareQueue(this.queue());
        admin.declareExchange(this.directExchange());
        admin.declareBinding(this.binding());
        return admin;
    }

    public String messagingToken() throws NoSuchAlgorithmException, InvalidKeySpecException {
        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(
                new PKCS8EncodedKeySpec(
                        Base64.getDecoder().decode(privateKey64)
                )
        );

        return "Bearer " + Jwts.builder()
                .setSubject("secure-messaging")
                .setIssuer("room-service")
                .setIssuedAt(Date.from(Instant.now()))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .compact();
    }




    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

}
