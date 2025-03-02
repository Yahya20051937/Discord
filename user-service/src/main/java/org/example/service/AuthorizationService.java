package org.example.service;


import org.example.config.WebClientConfig;
import org.example.dto.AuthorizationRequest;
import org.example.dto.JwtDto;
import org.example.model.Jwt;
import org.example.model.User;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AuthorizationService {
    private static final String REFRESH_TOKEN_ENDPOINT = "http://keycloak-discord:8180/realms/discord/protocol/openid-connect/token";

    @Autowired
    WebClientConfig webClientConfig;

    @Autowired
    DecodingService decodingService;

    public ResponseEntity<Jwt> authorize(AuthorizationRequest authorizationRequest){
        Keycloak keycloakInstance = authorizationRequest.toKeycloak();
        if (keycloakInstance != null)
            try {
                User user = decodingService.getUserFromJwt(keycloakInstance.tokenManager().getAccessToken());
                return new ResponseEntity<>(new Jwt(keycloakInstance.tokenManager().getAccessToken(), user.getUsername(), user.getHasImage()), HttpStatusCode.valueOf(200));
            } catch (Exception e){
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

        return new ResponseEntity<>(HttpStatusCode.valueOf(401));
    }

    public ResponseEntity<Jwt> refresh(String refreshToken){
        try {
            JwtDto jwt = webClientConfig.externalClient()
                    .post()
                    .uri(REFRESH_TOKEN_ENDPOINT)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(this.refreshTokenRequestBody(refreshToken)).retrieve()
                    .bodyToMono(JwtDto.class)
                    .block();

            if (jwt != null){
                User user = decodingService.getUserFromJwt(jwt);
                return new ResponseEntity<>(new Jwt(jwt, user.getUsername(), user.getHasImage()), HttpStatus.OK);
            }
            else
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

    }

    private BodyInserters.FormInserter<String> refreshTokenRequestBody(String refreshToken){
        return BodyInserters.fromFormData("grant_type", "refresh_token")
                .with("client_id", "discord-client")
                .with("client_secret", "oyRQfgbXdfnNYoSxKvIlHnDdpxO5fdfQ")
                .with("refresh_token", refreshToken);
    }

}
