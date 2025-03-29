package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MyRequestTest {

    @Autowired
    private MyURL myURL;
    @Autowired
    private Auth auth;

    private String myUrl;
    @Autowired
    MyRequest myRequest;

    @BeforeEach
    void setUp() {
        myUrl = myURL.setUrl(DocType.Document_ЗаказНаПроизводство, FieldType.ВидОперации, "Сборка");
    }

    @Test
    void TestdoRequest() throws IOException {

        URI uri = URI.create(myUrl);
        URL url = uri.toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Basic " + auth.getAuthInfo());
        int responseCode = connection.getResponseCode();

        assertEquals(200, responseCode);
    }

    @Test
    void TestdoRequest2() throws IOException {
        myRequest.setUrl(myUrl);
        String response = myRequest.doRequest();
        assertNotNull(response, "Response не должен быть null");
        assertFalse(response.isEmpty(), "Response не должен быть пустым");
    }
}
