package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@SpringBootTest
class AuthTest {

    @Autowired
    public Auth auth;

    @Test
    void testgetAuthInfo() {

        String authData = auth.getAuthInfo();
        assertNotNull(authData, "AuthData не может быть null");
        assertFalse(authData.isEmpty(), "AuthData не должен быть пустым");
    }
}
