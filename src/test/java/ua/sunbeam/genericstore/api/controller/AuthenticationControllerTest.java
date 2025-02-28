package ua.sunbeam.genericstore.api.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.ResponseEntity;
import ua.sunbeam.genericstore.model.LocalUser;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class AuthenticationControllerTest {

    @Autowired
    private AuthenticationController authenticationController;


    @Test
    public void AuthenticationController_RegisterWithBadCredentials_ReturnsBadRequest() {
        //ResponseEntity<> result =

    }

}
