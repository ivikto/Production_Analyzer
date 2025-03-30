package org.example;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;

@Slf4j
@Getter
@Component
public class MyRequest {

    private int responseCode;
    private final Auth auth;
    @Setter
    @Getter
    private String url;

    @Autowired
    public MyRequest(Auth auth) {
        this.auth = auth;
    }

    public String doRequest() {
        StringBuilder response = new StringBuilder();
        String line;
        try {
            URI uri = URI.create(url);
            URL url = uri.toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Authorization", "Basic " + auth.getEncodedAuth());

            responseCode = connection.getResponseCode();


            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            } else {
                log.error("Request error, Response code: " + responseCode);
            }

        } catch (MalformedURLException e) {
            log.error("Не корректный URL: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response.toString();

    }

}
