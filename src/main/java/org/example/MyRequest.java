package org.example;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Getter
@Component
public class MyRequest {
    public static Logger log = LoggerFactory.getLogger(Main.class);
    private URL url;
    private int responseCode;
    @Autowired
    private Auth auth;

    public String doRequest(String myUrl) {
        StringBuilder response = new StringBuilder();
        String line;
        try {
            this.url = new URL(myUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            log.info("Login: " + auth.getAuth());

            connection.setRequestProperty("Authorization", "Basic " + auth.getAuthInfo());

            responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
            }

        } catch (MalformedURLException e) {
            log.error("Не корректный URL: " + e.getMessage());
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response.toString();

    }

}
