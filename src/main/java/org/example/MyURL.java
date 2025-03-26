package org.example;

import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class MyURL {
    private String baseUrl = "https://1c.svs-tech.pro/UNF/odata/standard.odata/";
    private String filter = "?$filter=";
    private String filterByValue;
    private String filerGuidValue;


    public String setUrl(DocType docType, String filterByValue, String filerGuidValue) {
        filterByValue = URLEncoder.encode(filterByValue, StandardCharsets.UTF_8);
        String type = URLEncoder.encode(docType.name(), StandardCharsets.UTF_8);

        String url = baseUrl + type + filter + filterByValue + " eq guid'" + filerGuidValue + "'&$format=json";


        return url.replaceAll(" ", "%20").replaceAll("'", "%27");
    }
}