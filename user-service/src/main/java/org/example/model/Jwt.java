package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.dto.JwtDto;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.AccessTokenResponse;

@Data
@AllArgsConstructor
public class Jwt {
    private String tokenType;
    private String accessToken;
    private String refreshToken;
    private String username;
    private Boolean hasImage;

    public Jwt(AccessTokenResponse tokenResponse, String username, Boolean hasImage){
        this.tokenType = tokenResponse.getTokenType();
        this.accessToken = tokenResponse.getToken();
        this.refreshToken = tokenResponse.getRefreshToken();
        this.username = username;
        this.hasImage = hasImage;
    }

    public Jwt(JwtDto jwtDto, String username, Boolean hasImage){
        this.accessToken = jwtDto.getAccess_token();
        this.refreshToken = jwtDto.getRefresh_token();
        this.tokenType = jwtDto.getToken_type();
        this.username = username;
        this.hasImage = hasImage;
    }
}
