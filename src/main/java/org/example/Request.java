package org.example;

import org.springframework.stereotype.Component;

@Component
public class Request {
    private String baseUrl = "https://1c.svs-tech.pro/UNF/odata/standard.odata/";


    public void setUrl(DocType docType) {
        String url = baseUrl + docType.toString();
        System.out.println(url);
    }
}