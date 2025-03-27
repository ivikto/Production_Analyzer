package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class JsonParse {
    @Getter
    private List<ZNP> znpList = new ArrayList<>();
    private MyRequest request;
    private TimeCalc timeCalc;
    private MyURL url;

    @Autowired
    public JsonParse(MyRequest request, TimeCalc timeCalc, MyURL url) {
        this.request = request;
        this.timeCalc = timeCalc;
        this.url = url;
    }

    //Парсим и находим значением полей производсв со статусом - В работе
    public void jsonParseProd(String json, Period period) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootArray = objectMapper.readTree(json);

        JsonNode valueArray = rootArray.get("value");
        for (JsonNode node : valueArray) {
            String refkey = node.get("Ref_Key").asText();
            String number = node.get("Number").asText();
            String date = node.get("Date").asText();
            String productName;

            JsonNode operations = node.path("Операции");
            Double time = 0d;
            Double TotalTime = time * 60;
            for (JsonNode operation : operations) {
                time += operation.get("Нормочасы").asDouble();
            }

            List<String> list = new ArrayList<>();
            JsonNode product = node.path("Продукция");
            for (JsonNode prod : product) {
                String result = request.doRequest(url.setUrl(DocType.Catalog_Номенклатура, "Ref_Key", prod.get("Номенклатура_Key").asText()));
                list.add(jsonParseNum(result));

            }
            LocalDateTime dateTime = LocalDateTime.parse(date);
            if (timeCalc.checkDate(dateTime, period)) {
                ZNP znp = new ZNP();
                znp.setRef_key(refkey);
                znp.setNumber(number);
                znp.setTotalTime(time);
                znp.setDate(dateTime);
                znp.setList(list);

                znpList.add(znp);
            }
        }
    }

    public String jsonParseNum(String json) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootArray = objectMapper.readTree(json.toString());
        //System.out.println(rootArray);
        JsonNode valueArray = rootArray.get("value");

        String name = "";
        for (JsonNode node : valueArray) {
            name = node.path("НаименованиеПолное").asText();
        }

        return name;
    }


}
