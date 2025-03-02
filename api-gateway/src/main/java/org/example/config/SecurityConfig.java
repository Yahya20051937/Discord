package org.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {
    private final static String ISSUER_URI = "http://keycloak-discord:8180/realms/discord/protocol/openid-connect/certs";

    @Bean
    SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http){
        http.csrf().disable();
        http.authorizeExchange(
                exchange ->
                        exchange
                                .pathMatchers("/api/role/auth/**").authenticated()
                                .pathMatchers("/api/server/auth/**").authenticated()
                                .pathMatchers("/api/room/auth/**").authenticated()
                                .pathMatchers("/api/text-chatting/auth/**").authenticated()
                                .anyExchange().permitAll()

        );
        http.oauth2ResourceServer(it -> it.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(){
        return NimbusJwtDecoder.withJwkSetUri(ISSUER_URI).build();
    }
}