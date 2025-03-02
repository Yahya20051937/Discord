package org.example.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
    @Bean
    public Keycloak keycloak(){
        return KeycloakBuilder.builder()
                .serverUrl("http://keycloak-discord:8180/")
                .realm("discord")
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId("discord-client")
                .clientSecret("oyRQfgbXdfnNYoSxKvIlHnDdpxO5fdfQ")
                .build();
    }
}
