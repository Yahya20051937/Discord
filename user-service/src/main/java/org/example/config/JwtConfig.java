package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtConfig {
    private final static String ISSUER_URI = "http://keycloak-discord:8180/realms/discord/protocol/openid-connect/certs";

    @Bean
    public JwtDecoder jwtDecoder() {return NimbusJwtDecoder.withJwkSetUri(ISSUER_URI).build();}
}
