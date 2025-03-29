package org.example;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@SpringBootApplication
@PropertySource("classpath:jsonPars.properties")
public class Main {


    private final MyURL myURL;
    private final ExcelWrite excelWrite;
    private final JsonParse jsonParse;
    private final MyRequest myRequest;
    private final Output output;

    @Getter
    public List<ZNP> znpList = new ArrayList<>();

    @Autowired
    public Main(MyURL myURL, ExcelWrite excelWrite, JsonParse jsonParse, MyRequest myRequest, Output output) {
        this.myURL = myURL;
        this.excelWrite = excelWrite;
        this.jsonParse = jsonParse;
        this.myRequest = myRequest;
        this.output = output;
    }

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(Main.class);
        Main main = context.getBean(Main.class);
        log.info("main start");
        LocalTime now = LocalTime.now();
        main.run();
        LocalTime finish = LocalTime.now();
        Duration duration = Duration.between(now, finish);
        log.info("Продолжительность операции: " + duration.toSeconds() + " сек");
    }

    public void run() {

        log.info("main run");
        String url = myURL.setUrl(DocType.Document_ЗаказНаПроизводство, FieldType.СостояниеЗаказа_Key, "4f5e06a1-5f73-11ed-a1fd-d2166770609f");
        log.info(url);
        myRequest.setUrl(url);
        String response = myRequest.doRequest();
        jsonParse.jsonParseProd(response, Period.Month);
        znpList = jsonParse.getZnpList();
        znpList = znpList.stream()
                .filter(znp -> !znp.isPosted())
                .toList();
        for (ZNP znp : znpList) {
            TimeCalc.calculateTime(znp);
            output.printResult(znp);
        }


        output.printRatio(znpList);
        excelWrite.createExcel(znpList);

    }
}