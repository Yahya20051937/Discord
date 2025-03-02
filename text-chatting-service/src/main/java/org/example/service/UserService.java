package org.example.service;

import com.nimbusds.jwt.JWTParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import org.example.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.prefs.BackingStoreException;

@Service
public class UserService {
    @Autowired
    WebClient webClient;

    @Value("${private-key}")
    private String privateKey64;

    private User getUser(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        return this.sendRequest(token);
    }

    public User getUser(String token){
        return this.sendRequest(token);
    }

    private User sendRequest(String token){
        return webClient.get()
                .uri("http://user-service/api/user/get")
                .header("Authorization", token)
                .retrieve()
                .bodyToMono(User.class)
                .block();
    }

    public String generateUploadMessageMediaToken(String messageId) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PrivateKey privateKey = KeyFactory.getInstance("RSA")
                .generatePrivate(
                        new PKCS8EncodedKeySpec(
                                Base64.getDecoder().decode(privateKey64)
                        )
                );
        return "Bearer " + Jwts.builder()
                .claim("messageId", messageId)
                .setIssuer("text-chatting-service")
                .setSubject("upload-message-media")
                .setIssuedAt(Date.from(Instant.now()))
                .signWith(SignatureAlgorithm.RS256, privateKey)
                .setExpiration(Date.from(Instant.now().plusSeconds(3600 * 24)))
                .compact(); // valid for 1 day.
    }

    public String getVideoIdFromUploadToken(String token){
        try {
            Map<String, Object> claims = JWTParser.parse(token.split(" ")[1]).getJWTClaimsSet().getClaims();
            return (String) claims.get("videoId");
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Boolean isClientAuthorizedToUpload(String token){
        return webClient.get()
                .uri("http://user-service/api/user/message-broker/authorized") // we are using the same uri that is used in the verification of message broocker
                .header("token", token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    public Boolean isMessageBrokerAuthorized(String token){
        return webClient.get()
                .uri("http://user-service/api/user/message-broker/authorized")
                .header("token", token)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block();
    }

    }
