package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.List;

@Slf4j
@Component
@PropertySource("classpath:jsonPars.properties")
public class Bot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    @Value("${botToken}")
    private String botToken;
    private TelegramClient telegramClient;
    private Output output;
    private ExcelWrite excelWrite;
    private Runner runner;

    @PostConstruct
    public void init() {
        if (botToken == null) {
            throw new IllegalStateException("botToken is null");
        }
        telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Autowired // Инъекция через сеттер
    public Bot(Output output, ExcelWrite excelWrite, Runner runner) {
        this.output = output;
        this.excelWrite = excelWrite;
        this.runner = runner;
    }


    @Override
    public void consume(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String message_text = update.getMessage().getText();
            long chat_id = update.getMessage().getChatId();

            if (message_text.equalsIgnoreCase("getinfo")) {
                log.info("Запрос GetInfo из Telegram");
                sendMessage("Выполняется запрос, ожидайте...", chat_id);
                runner.run();

                List<ZNP> znpList = runner.getZnpList();
                for (ZNP znp : znpList) {
                    String result = output.getResult(znp);
                    sendMessage(result, chat_id);
                }
                String ratio = output.getRatioMessage(znpList);
                sendMessage(ratio, chat_id);
                sendFile(chat_id);

            } else {
                sendMessage(message_text, chat_id);
            }

        }

    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    public void sendMessage(String message_text, long chat_id) {
        SendMessage message = SendMessage // Create a message object
                .builder()
                .chatId(chat_id)
                .text(message_text)
                .build();

        try {
            telegramClient.execute(message); // Sending our message object to user
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }

    public void sendFile(long chatId) {
        try {
           File file = new File(excelWrite.getFilePath());
            // Создаем InputFile из File
            InputFile inputFile = new InputFile(file);

            // Создаем SendDocument через builder
            SendDocument document = SendDocument.builder()
                    .chatId(String.valueOf(chatId)) // chatId должен быть String
                    .document(inputFile)
                    .caption("Ваш файл с отчетом")
                    .build();

            telegramClient.execute(document);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке файла", e);
            sendMessage("Не удалось отправить файл", chatId);
        }
    }


}


