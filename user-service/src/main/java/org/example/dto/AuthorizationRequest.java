package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationRequest {
    private String username;
    private String password;

    public AuthorizationRequest(RegistrationRequest registrationRequest){
        this.username = registrationRequest.getUsername();
        this.password = registrationRequest.getPassword();
    }

    public Keycloak toKeycloak(){
        try {
            return KeycloakBuilder.builder()
                    .serverUrl("http://keycloak-discord:8180/")
                    .realm("discord")
                    .grantType(OAuth2Constants.PASSWORD)
                    .clientId("discord-client")
                    .clientSecret("oyRQfgbXdfnNYoSxKvIlHnDdpxO5fdfQ")
                    .username(this.username)
                    .password(this.password)
                    .build();
        } catch (Exception e) {return null;}
    }
}
