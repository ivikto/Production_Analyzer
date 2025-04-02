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
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.ArrayList;
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
        registerCommands(); // Регистрируем команды при инициализации
    }

    @Autowired
    public Bot(Output output, ExcelWrite excelWrite, Runner runner) {
        this.output = output;
        this.excelWrite = excelWrite;
        this.runner = runner;
    }

    private void registerCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/start", "Начать работу с ботом"));
        commands.add(new BotCommand("/getinfo", "Получить информацию и отчет"));

        try {
            telegramClient.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка при регистрации команд меню", e);
        }
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.equalsIgnoreCase("/start")) {
                sendWelcomeMessage(chatId);
            }
            else if (messageText.equalsIgnoreCase("/getinfo") || messageText.equalsIgnoreCase("getinfo")) {
                handleGetInfoCommand(chatId);
            } else {
                log.info("Боту пришло сообщение: " + messageText);
            }
        }
    }

    private void sendWelcomeMessage(long chatId) {
        String welcomeText = "Добро пожаловать!\n\n" +
                "Доступные команды:\n" +
                "/getinfo - Получить информацию и отчет\n\n" +
                "Или нажмите кнопку ниже:";

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(welcomeText)
                .replyMarkup(createMainMenuKeyboard())
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке приветственного сообщения", e);
        }
    }

    private ReplyKeyboardMarkup createMainMenuKeyboard() {
        // Создаем список рядов кнопок
        KeyboardRow row = new KeyboardRow();
        row.add("📊 Получить отчет");

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        // Создаем клавиатуру через билдер
        return ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .resizeKeyboard(true)  // Подгоняем размер кнопок
                .oneTimeKeyboard(false) // Клавиатура остается после нажатия
                .build();
    }

    private void handleGetInfoCommand(long chatId) {
        log.info("Запрос GetInfo из Telegram");
        sendMessage("Выполняется запрос, ожидайте...", chatId);

        try {
            runner.run();
            List<ZNP> znpList = runner.getZnpList();

            for (ZNP znp : znpList) {
                String result = output.getResult(znp);
                sendMessage(result, chatId);
            }

            String ratio = output.getRatioMessage(znpList);
            sendMessage(ratio, chatId);
            sendFile(chatId);

        } catch (Exception e) {
            log.error("Ошибка при выполнении команды getinfo", e);
            sendMessage("Произошла ошибка при выполнении запроса", chatId);
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


