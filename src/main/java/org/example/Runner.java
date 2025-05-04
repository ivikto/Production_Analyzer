package org.example;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class Runner {

    private final MyURL myURL;
    private final ExcelWrite excelWrite;
    private final JsonParse jsonParse;
    private final MyRequest myRequest;
    private final Output output;

    @Getter
    private List<ZNP> znpList = new ArrayList<>();

    @Autowired
    public Runner(MyURL myURL, ExcelWrite excelWrite, JsonParse jsonParse,
                  MyRequest myRequest, Output output) {
        this.myURL = myURL;
        this.excelWrite = excelWrite;
        this.jsonParse = jsonParse;
        this.myRequest = myRequest;
        this.output = output;
    }

    public List<ZNP> run() {
        log.info("Starting production analysis");
        LocalTime startTime = LocalTime.now();

        try {
            String url = myURL.setUrl(DocType.Document_ЗаказНаПроизводство,
                    FieldType.СостояниеЗаказа_Key, "4f5e06a1-5f73-11ed-a1fd-d2166770609f");
            log.debug("Request URL: {}", url);

            myRequest.setUrl(url);
            String response = myRequest.doRequest();

            jsonParse.jsonParseProd(response, Period.Month);
            znpList = jsonParse.getZnpList().stream()
                    .filter(znp -> !znp.isPosted())
                    .peek(znp -> {
                        TimeCalc.calculateTime(znp);
                        output.printResult(znp);
                    })
                    .toList();


            output.printRatio(znpList);
            excelWrite.createExcel(znpList);

            log.info("Analysis completed in {} seconds",
                    Duration.between(startTime, LocalTime.now()).toSeconds());
        } catch (Exception e) {
            log.error("Error during production analysis", e);
            throw new RuntimeException("Failed to execute production analysis", e);
        }
        return znpList;
    }
}
