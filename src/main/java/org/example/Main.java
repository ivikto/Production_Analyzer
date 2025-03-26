package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@PropertySource("classpath:jsonPars.properties")
public class Main {
    @Value(value = "${jsonPars.login}")
    private String login; // Логин и пароль
    @Value(value = "${jsonPars.pass}")
    private String pass; // Логин и пароль
    private String auth = login + ":" + pass;
    private String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
    @Autowired
    private Main main;
    @Autowired
    private Request request;
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
        ApplicationContext context = new AnnotationConfigApplicationContext("org.example");
        Main main = context.getBean(Main.class);

//        try {
//            String json = main.requestProd();
//            main.jsonParseProd(json);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        for (ZNP znp : znpList) {
//            calculateTime(znp);
//        }
//        System.out.printf("Нарушены сроки по %d из %d производств", violation, znpList.size());
        Request request = context.getBean(Request.class);
        request.setUrl(DocType.Catalog_Номенклатура);




    }

    // Выполняем запрос к 1С - Document_ЗаказНаПроизводство
    public String requestProd() throws IOException {
        String line = "";
        StringBuilder response = new StringBuilder();
        String numeric = URLEncoder.encode("ЗаказНаПроизводство", StandardCharsets.UTF_8);
        String refKey = "4f5e06a1-5f73-11ed-a1fd-d2166770609f";
        String baseUrl = "https://1c.svs-tech.pro/UNF/odata/standard.odata/Document_" + numeric + "?$filter=СостояниеЗаказа_Key%20eq%20guid%27" + refKey + "%27&$format=json";
        //url sample: https://1c.svs-tech.pro/UNF/odata/standard.odata/Document_ЗаказНаПроизводство?$filter=СостояниеЗаказа_Key%20eq%20guid%274f5e06a1-5f73-11ed-a1fd-d2166770609f%27&$format=json

        // Создаём URL-объект
        URL url = new URL(baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // Устанавливаем метод запроса
        connection.setRequestMethod("GET");

        connection.setRequestProperty("Authorization", "Basic " + main.encodedAuth);

        //log.info("Auth value: " + main.getAuth());

        int responseCode = connection.getResponseCode();


        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));


            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
        }
        return response.toString();
    }

    // Выполняем запрос к 1С Catalog_Номенклатура
    public String requestNum(String ref) throws IOException {
        String line = "";
        StringBuilder response = new StringBuilder();
        String numeric = URLEncoder.encode("Номенклатура", StandardCharsets.UTF_8);
        String baseUrl = "https://1c.svs-tech.pro/UNF/odata/standard.odata/Catalog_" + numeric + "?$filter=Ref_Key%20eq%20guid%27" +ref + "%27&$format=json";
        //url sample: https://1c.svs-tech.pro/UNF/odata/standard.odata/Document_ЗаказНаПроизводство?$filter=СостояниеЗаказа_Key%20eq%20guid%274f5e06a1-5f73-11ed-a1fd-d2166770609f%27&$format=json

        //System.out.println(baseUrl);
        // Создаём URL-объект
        URL url = new URL(baseUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // Устанавливаем метод запроса
        connection.setRequestMethod("GET");

        connection.setRequestProperty("Authorization", "Basic " + main.encodedAuth);

        //log.info("Auth value: " + main.getAuth());

        int responseCode = connection.getResponseCode();

        if (responseCode == 200) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));


            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
        }
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootArray = objectMapper.readTree(response.toString());
        //System.out.println(rootArray);
        JsonNode valueArray = rootArray.get("value");

        String name = "";
        for (JsonNode node : valueArray) {
            name = node.path("НаименованиеПолное").asText();
        }

        return name;
    }

    //Парсим и находим значением полей производсв со статусом - В работе
    public void jsonParseProd(String json) throws JsonProcessingException {

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
            //System.out.println(time);
            for (JsonNode operation : operations) {
                time += operation.get("Нормочасы").asDouble();
            }

            List<String> list = new ArrayList<>();
            JsonNode product = node.path("Продукция");
            for (JsonNode prod : product) {
                try {
                    list.add(requestNum(prod.get("Номенклатура_Key").asText()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            }
            //System.out.printf("Ref_Key: %s Number: %s Date: %s Нормочасы: %f\n", refkey, number, date, time);
            ZNP znp = new ZNP();
            znp.setRef_key(refkey);
            znp.setNumber(number);
            znp.setTotalTime(time);
            LocalDateTime dateTime = LocalDateTime.parse(date);
            znp.setDate(dateTime);
            znp.setList(list);
            znpList.add(znp);
        }
    }


    public static void calculateTime(ZNP znp) {
        try {
            // Получаем общее время в минутах
            long totalMinutes = (long)(znp.getTotalTime() * 60);

            LocalDateTime start = znp.getDate();
            LocalDateTime deadline = calculateWorkingDeadline(start, totalMinutes);

            if (LocalDateTime.now().isAfter(deadline)) {
                System.out.println(znp.getNumber() + " Создан: " + formatDateTime(start) +
                        " Должен быть завершен: " + formatDateTime(deadline) +
                        " Времени выделено: " + znp.getTotalTime() + " часа" +
                        " НАРУШЕНИЕ" +
                        " Изделия: " + znp.getList());
                violation++;
            } else {
                System.out.println(znp.getNumber() + " Создан: " + formatDateTime(start) +
                        " Должен быть завершен: " + formatDateTime(deadline) +
                        " Времени выделено: " + znp.getTotalTime() + " часа" +
                        " НОРМА" +
                        " Изделия: " + znp.getList());
            }
        } catch (Exception e) {
            System.err.println("Ошибка при расчете времени для " + znp.getNumber() + ": " + e.getMessage());
        }
    }

    private static LocalDateTime calculateWorkingDeadline(LocalDateTime start, long remainingMinutes) {
        LocalDateTime current = start;
        int maxDays = 365 * 10; // Защита от бесконечного цикла (10 лет)
        int daysPassed = 0;

        while (remainingMinutes > 0 && daysPassed++ < maxDays) {
            // Проверяем, рабочий ли это день (пн-пт)
            if (isWorkDay(current.toLocalDate())) {
                LocalTime workStart = LocalTime.of(8, 30);
                LocalTime workEnd = LocalTime.of(17, 0);

                // Если текущее время до начала рабочего дня
                if (current.toLocalTime().isBefore(workStart)) {
                    current = LocalDateTime.of(current.toLocalDate(), workStart);
                    continue;
                }

                // Если текущее время после конца рабочего дня
                if (current.toLocalTime().isAfter(workEnd)) {
                    current = LocalDateTime.of(current.toLocalDate().plusDays(1), workStart);
                    continue;
                }

                // Рассчитываем оставшееся время в текущем рабочем дне
                long minutesLeftInDay = ChronoUnit.MINUTES.between(current.toLocalTime(), workEnd);
                long minutesToUse = Math.min(remainingMinutes, minutesLeftInDay);

                remainingMinutes -= minutesToUse;
                current = current.plusMinutes(minutesToUse);

                // Если использовали все минуты дня, переходим к следующему рабочему дню
                if (minutesToUse == minutesLeftInDay) {
                    current = LocalDateTime.of(current.toLocalDate().plusDays(1), workStart);
                }
            } else {
                // Это не рабочий день, переходим к следующему дню
                current = LocalDateTime.of(current.toLocalDate().plusDays(1), LocalTime.of(8, 30));
            }
        }

        if (daysPassed >= maxDays) {
            throw new RuntimeException("Превышен максимальный срок расчета (10 лет)");
        }

        return current;
    }

    private static boolean isWorkDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    private static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }
}