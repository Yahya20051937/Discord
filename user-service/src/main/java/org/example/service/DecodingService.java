package org.example.service;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import com.nimbusds.jwt.JWTParser;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.example.dto.JwtDto;
import org.example.model.User;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

@Service
public class DecodingService {
    @Autowired
    JwtDecoder jwtDecoder;


    @Value("${public-key}")
    private String publicKeyPem;

    public User getUserFromJwt(String token) throws ParseException {
        Map<String, Object> claims = JWTParser.parse(token.split(" ")[1]).getJWTClaimsSet().getClaims();
        String username = (String) claims.get("preferred_username");
        String email = (String) claims.get("email");

        return User.builder()
                .username(username)
                .email(email)
                .hasImage(this.getHasImageFromJwt(token))
                .build();
    }

    public Boolean getHasImageFromJwt(String token) throws ParseException {
        Map<String, Object> claims = JWTParser.parse(token.split(" ")[1]).getJWTClaimsSet().getClaims();
        try {
            return (Boolean) claims.get("hasImage");
        } catch (Exception e){
            return false;
        }


    }

    public User getUserFromJwt(JwtDto jwtDto) throws ParseException {
        return this.getUserFromJwt(jwtDto.getToken_type() + " " + jwtDto.getAccess_token());
    }

    public User getUserFromJwt(AccessTokenResponse tokenResponse) throws ParseException {
        return this.getUserFromJwt(tokenResponse.getTokenType() + " " + tokenResponse.getToken());
    }

    public Boolean isMessageBrokerAuthorized(String token){
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyPem)));

            Algorithm algorithm = Algorithm.RSA256(rsaPublicKey);
            JWT.require(algorithm)
                    .build()
                    .verify(token.split(" ")[1]);
            return true;
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }
}


