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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.*;
import java.util.function.BiFunction;

@Configuration
public class RabbitConfig {

    public static String ROOM_TEXT_CHATTING_EXCHANGE = "ROOM_TEXT_CHATTING-EXCHANGE";

    public static String ROOM_TEXT_CHATTING_QUEUE = "ROOM_TEXT_CHATTING-QUEUE";

    public static String ROOM_TEXT_CHATTING_ROUTING_KEY = "ROOM_TEXT_CHATTING-ROUTING.KEY";

    @Bean
    public Queue queue(){
        return new Queue(ROOM_TEXT_CHATTING_QUEUE, true);
    }

    @Bean
    DirectExchange directExchange(){
        return new DirectExchange(ROOM_TEXT_CHATTING_EXCHANGE);
    }


    @Bean
    Binding binding(){
        return BindingBuilder.bind(queue())
                .to(directExchange())
                .with(ROOM_TEXT_CHATTING_ROUTING_KEY);
    }


    @Value("${private-key}")
    private String privateKey64;

    @Autowired
    WebClientConfig webClientConfig;

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
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory){
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory){
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.declareQueue(queue());
        rabbitAdmin.declareExchange(directExchange());
        rabbitAdmin.declareBinding(binding());
        return rabbitAdmin;
    }


    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
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



    public List<String> getAllQueues(BiFunction<String, String, Boolean> filter1, String...args){
        try {
            List<Map<String, Object>> maps =  webClientConfig.externalClient().get()
                    .uri("http://rabbitmq-discord:15672/api/queues")
                    .header("Authorization", "Basic " + Base64.getEncoder().encodeToString("yahya:Wydad3719".getBytes()))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
            assert maps != null;
            return  maps.stream()
                    .map(it -> (String) it.get("name"))
                    .filter(it -> {
                        try {
                            return filter1.apply(it, args[0]);
                        } catch (IndexOutOfBoundsException  e){
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .toList();
        } catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }


}
