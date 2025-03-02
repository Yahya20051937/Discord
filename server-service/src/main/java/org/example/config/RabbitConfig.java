package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.bouncycastle.jcajce.BCFKSLoadStoreParameter;
import org.springframework.amqp.core.*;
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
    public static String SERVER_ROLE_EXCHANGE = "SERVER-ROLE-EXCHANGE";
    public static String SERVER_ROOM_EXCHANGE = "SERVER-ROOM-EXCHANGE";

    public static String SERVER_ROLE_QUEUE = "SERVER-ROLE-QUEUE";
    public static String SERVER_ROOM_QUEUE = "SERVER-ROOM-QUEUE";

    public static String SERVER_ROLE_ROUTING_KEY = "SERVER-ROLE.ROUTING.KEY";
    public static String SERVER_ROOM_ROUTING_KEY = "SERVER-ROOM.ROUTING.KEY";

    @Value("${private-key}")
    private String privateKey64;

    @Autowired
    WebClientConfig webClientConfig;


    @Bean
    Queue queue1(){
        return new Queue(SERVER_ROLE_QUEUE, true);
    }

    @Bean
    Queue queue2(){
        return new Queue(SERVER_ROOM_QUEUE, true);
    }

    @Bean
    DirectExchange directExchange1(){
        return new DirectExchange(SERVER_ROLE_EXCHANGE);
    }

    @Bean
    DirectExchange directExchange2(){
        return new DirectExchange(SERVER_ROOM_EXCHANGE);
    }

    @Bean
    Binding binding1(){
        return BindingBuilder.bind(queue1())
                .to(directExchange1())
                .with(SERVER_ROLE_ROUTING_KEY);
    }

    @Bean
    Binding binding2(){
        return BindingBuilder.bind(queue2())
                .to(directExchange2())
                .with(SERVER_ROOM_ROUTING_KEY);
    }

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
        RabbitAdmin admin = new RabbitAdmin(connectionFactory);

        admin.declareQueue(queue1());
        admin.declareExchange(directExchange1());
        admin.declareBinding(binding1());

        admin.declareQueue(queue2());
        admin.declareExchange(directExchange2());
        admin.declareBinding(binding2());


        return admin;
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
                .setIssuer("server-service")
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
