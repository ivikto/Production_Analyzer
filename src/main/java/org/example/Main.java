package org.example;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
@SpringBootApplication
@PropertySource("classpath:jsonPars.properties")
public class Main {
    @Value(value = "${login}")
    private String login; // Логин и пароль
    @Value(value = "${pass}")
    private String pass; // Логин и пароль
    private String auth = login + ":" + pass;
    private String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

    @Autowired
    private Main main;
    @Autowired
    private MyURL myURL;
    @Autowired
    private ExcelWrite excelWrite;
    @Autowired
    private JsonParse jsonParse;
    @Autowired
    private TimeCalc timeCalc;
    @Autowired
    private MyRequest myRequest;
    @Autowired
    private ZNP znp;

    public static List<ZNP> znpList = new ArrayList<>();
    //Поле для подсчета нарушений по производствам
    public static int violation = 0;
    //Логирование
    public static Logger log = LoggerFactory.getLogger(Main.class);

    @PostConstruct
    public void init() {
        // Инициализируем поля после внедрения значений
        this.auth = login + ":" + pass;
        this.encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    }

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(Main.class);
        Main main = context.getBean(Main.class);
        main.run();
    }

    public void run() {
        String url = myURL.setUrl(DocType.Document_ЗаказНаПроизводство, "СостояниеЗаказа_Key", "4f5e06a1-5f73-11ed-a1fd-d2166770609f");
        System.out.println(url);

        try {
            jsonParse.jsonParseProd(myRequest.doRequest(url), Period.Month);
            System.out.println("Код ответа сервера: " + myRequest.getResponseCode());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        znpList = jsonParse.getZnpList();
        for (ZNP znp : znpList) {
            TimeCalc.calculateTime(znp);
            Output.printResult(znp);
        }

        Output.printRatio();
        excelWrite.createExcel(znpList);
    }
}