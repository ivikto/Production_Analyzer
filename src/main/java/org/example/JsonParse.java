package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
public class JsonParse {
    @Getter
    private List<ZNP> znpList;
    private final MyRequest request;
    private final TimeCalc timeCalc;
    private final MyURL url;


    @Autowired
    public JsonParse(MyRequest request, TimeCalc timeCalc, MyURL url) {
        this.request = request;
        this.timeCalc = timeCalc;
        this.url = url;
    }

    //Парсим и находим значением полей производсв со статусом - В работе
    public void jsonParseProd(String json, Period period) {
        znpList = new ArrayList<>();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootArray;
        try {
            rootArray = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

        JsonNode valueArray = rootArray.get("value");
        for (JsonNode node : valueArray) {
            String refkey = node.get("Ref_Key").asText();
            String number = node.get("Number").asText();
            String date = node.get("Date").asText();
            String posted = "";

            JsonNode operations = node.path("Операции");
            double time = 0d;
            for (JsonNode operation : operations) {
                time += operation.get("Нормочасы").asDouble();
            }

            List<String> list = new ArrayList<>();
            JsonNode product = node.path("Продукция");
            for (JsonNode prod : product) {
                // До добавления трэдов 22.2 секунды / после 14 секунд

                @Cleanup
                ExecutorService executor = Executors.newFixedThreadPool(2);
                MyRequest request1 = new MyRequest(request.getAuth());
                MyRequest request2 = new MyRequest(request.getAuth());

                request1.setUrl(url.setUrl(DocType.Catalog_Номенклатура, FieldType.Ref_Key, prod.get("Номенклатура_Key").asText()));
                Future<String> res1 = executor.submit(new MyThread(request1));
                request2.setUrl(url.setUrl(DocType.Document_СборкаЗапасов, FieldType.ЗаказНаПроизводство_Key, refkey));
                Future<String> res2 =  executor.submit(new MyThread(request2));

                String result1 = null;
                String result2 = null;
                try {
                    result1 = res1.get();
                    result2 = res2.get();

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }

                try {
                    list.add(jsonParseData(result1, "НаименованиеПолное"));
                    posted = jsonParseData(result2, "Posted");
                    date = jsonParseData(result2, "Date");

                } catch (JsonProcessingException e) {
                    log.error(e.getMessage());
                    throw new RuntimeException(e);
                }


            }

            LocalDateTime dateTime = LocalDateTime.parse(date);
            if (timeCalc.checkDate(dateTime, period)) {
                ZNP znp = new ZNP();
                znp.setRef_key(refkey);
                znp.setNumber(number);
                znp.setTotalTime(time);
                znp.setDate(dateTime);
                znp.setList(list);
                znp.setPosted(Boolean.parseBoolean(posted));

                znpList.add(znp);
            }
        }
    }

    public String jsonParseData(String json, String field) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootArray = objectMapper.readTree(json);
        JsonNode valueArray = rootArray.get("value");

        String data = "";
        for (JsonNode node : valueArray) {
            data = node.path(field).asText();
        }

        return data;
    }


}
