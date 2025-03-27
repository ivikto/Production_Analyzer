package org.example;

import lombok.Getter;
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

    @Autowired
    public MyRequest(Auth auth) {
        this.auth = auth;
    }


    public String doRequest(String myUrl) {
        StringBuilder response = new StringBuilder();
        String line;
        try {
            URI uri = URI.create(myUrl);
            URL url = uri.toURL();

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            connection.setRequestProperty("Authorization", "Basic " + auth.getAuthInfo());

            responseCode = connection.getResponseCode();
            //log.info("Response Code : " + responseCode);

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            }

        } catch (MalformedURLException e) {
            log.error("Не корректный URL: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response.toString();

    }

}
