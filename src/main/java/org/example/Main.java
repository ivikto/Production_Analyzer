package org.example;

import lombok.Cleanup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
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
    private Bot bot;

    @Getter
    public List<ZNP> znpList = new ArrayList<>();

    @Autowired // Добавляем setter для Bot
    public void setBot(Bot bot) {
        this.bot = bot;
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
        main.bot.init();
        @Cleanup
        Scanner scanner = new Scanner(System.in);
        while (true) {
            log.info("Введите команду GetInfo: ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("getinfo")) {
                main.run();
            } else if (input.equalsIgnoreCase("exit")) {
                break;

            }

        }





    }

    public void run() {
        log.info("main run");
        LocalTime now = LocalTime.now();
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
        LocalTime finish = LocalTime.now();
        Duration duration = Duration.between(now, finish);
        log.info("Продолжительность операции: " + duration.toSeconds() + " сек");
    }

}