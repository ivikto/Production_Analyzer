package org.example;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@Slf4j
@SpringBootApplication
@PropertySource("classpath:jsonPars.properties")
public class Main {

    private final Runner runner;

    @Autowired
    public Main(Runner runner) {
        this.runner = runner;

    }

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(Main.class, args);
        Main main = context.getBean(Main.class);
        @Cleanup
        Scanner scanner = new Scanner(System.in);
        while (true) {
            log.info("Введите команду GetInfo: ");
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("getinfo")) {
                main.runner.run();
            } else if (input.equalsIgnoreCase("exit")) {
                break;

            }

        }





    }



}