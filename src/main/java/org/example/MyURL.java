package org.example;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@PropertySource("classpath:jsonPars.properties")
public class MyURL {
    @Value(value = "${baseUrl}")
    public String baseUrl;

    public String setUrl(DocType docType, FieldType fieldType, String filerGuidValue) {

        String filter = "?$filter=";
        String filterByValue = URLEncoder.encode(fieldType.name(), StandardCharsets.UTF_8);
        String type = URLEncoder.encode(docType.name(), StandardCharsets.UTF_8);
        String guid = " eq guid'";

        if (!fieldType.name().contains("Key")) {
            guid = " eq '";
        }

        String url = baseUrl + type + filter + filterByValue + guid + filerGuidValue + "'&$format=json";

        return url.replaceAll(" ", "%20").replaceAll("'", "%27");
    }
}