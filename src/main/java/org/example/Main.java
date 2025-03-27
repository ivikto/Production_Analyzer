package org.example;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
    public static List<ZNP> znpList;

    static {
        znpList = new ArrayList<>();
    }

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
        main.run();
    }

    public void run() {

        log.info("main run");
        String url = myURL.setUrl(DocType.Document_ЗаказНаПроизводство, "СостояниеЗаказа_Key", "4f5e06a1-5f73-11ed-a1fd-d2166770609f");
        log.info(url);
        String response = myRequest.doRequest(url);
        jsonParse.jsonParseProd(response, Period.Month);
        znpList = jsonParse.getZnpList();
        for (ZNP znp : znpList) {
            TimeCalc.calculateTime(znp);
            Output.printResult(znp);
        }

        output.printRatio(znpList);
        excelWrite.createExcel(znpList);
    }
}