package org.example;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@PropertySource("classpath:jsonPars.properties")
public class Auth {
    @Value(value = "${login}")
    private String login; // Логин и пароль
    @Value(value = "${pass}")
    private String pass; // Логин и пароль
    @Getter
    private String auth = login + ":" + pass;
    private String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    private final Environment environment;

    @Autowired
    public Auth(Environment environment) {
        this.environment = environment;
    }


    @PostConstruct
    public void init() {
        // Инициализируем поля после внедрения значений
        this.auth = login + ":" + pass;
        this.encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    public String getAuthInfo() {
        return encodedAuth;
    }
}
