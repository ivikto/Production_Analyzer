package org.example;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
@PropertySource("classpath:jsonPars.properties")
public class Auth {
    @Value(value = "${login}")
    private String login; // Логин и пароль
    @Value(value = "${pass}")
    private String pass; // Логин и пароль
    @Getter
    private String authInfo = login + ":" + pass;
    @Getter
    private String encodedAuth = Base64.getEncoder().encodeToString(authInfo.getBytes(StandardCharsets.UTF_8));


    @PostConstruct
    public void init() {
        // Инициализируем поля после внедрения значений
        this.authInfo = login + ":" + pass;
        this.encodedAuth = Base64.getEncoder().encodeToString(authInfo.getBytes(StandardCharsets.UTF_8));
    }

    public String getAuthInfo() {
        return encodedAuth;
    }
}
