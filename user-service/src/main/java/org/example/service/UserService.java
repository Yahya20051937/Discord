package org.example.service;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class UserService {
    @Autowired
    Keycloak keycloak;

    public void editHasImageAttribute(String username, Boolean to){
        try {
            UserRepresentation user = keycloak.realm("discord")
                    .users()
                    .searchByUsername(username, true)
                    .get(0);
            user.setAttributes(Map.of("hasImage", List.of(to.toString())));
            keycloak.realm("discord")
                    .users()
                    .get(user.getId())
                    .update(user);
        } catch (NoSuchElementException e){
            e.printStackTrace();
        }


    }

    public Boolean doesUserHaveImage(String username){
        try {
            return Boolean.valueOf(
                    keycloak.realm("discord")
                            .users()
                            .searchByUsername(username, true)
                            .get(0)
                            .getAttributes()
                            .get("hasImage")
                            .get(0)
            );

        } catch (NoSuchElementException e){
            e.printStackTrace();
            return false;
        }
    }
}
