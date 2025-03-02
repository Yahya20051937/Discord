package org.example.service;


import org.example.dto.AuthorizationRequest;
import org.example.dto.RegistrationRequest;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;

@Service
public class RegistrationService {
    private static final String REALM_NAME = "discord";
    @Autowired
    Keycloak keycloak;
    @Autowired
    AuthorizationService authorizationService;

    @Autowired
    MediaService mediaService;


    public ResponseEntity<?> register(RegistrationRequest request){
        UserRepresentation userRepresentation = request.toRepresentation();
        Response response = keycloak.realm(REALM_NAME)
                .users()
                .create(userRepresentation);
        if (response.getStatus() == 201) {
            mediaService.createEmptyFile(request.getUsername());
            return authorizationService.authorize(new AuthorizationRequest(request));
        }
        return new ResponseEntity<>(HttpStatus.valueOf(response.getStatus()));
    }
}
