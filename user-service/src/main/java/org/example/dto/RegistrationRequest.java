package org.example.dto;

import lombok.Data;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.ManagementPermissionReference;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class RegistrationRequest {
    private String username;
    private String email;
    private String password;

    public UserRepresentation toRepresentation(){
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(this.username);
        userRepresentation.setEmail(this.email);
        userRepresentation.setEnabled(true);
        userRepresentation.setAttributes(Map.of("hasImage", List.of("false")));

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setTemporary(false);
        credentialRepresentation.setValue(this.password);
        userRepresentation.setCredentials(List.of(credentialRepresentation));
        return userRepresentation;
    }
}
