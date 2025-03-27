package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static org.example.TimeCalc.formatDateTime;

@Component
public class Output {

    private static TimeCalc timeCalc;
    private static Main main;
    private static ZNP znp;

    @Autowired
    public Output(TimeCalc timeCalc) {
        this.timeCalc = timeCalc;
        this.main = main;
        this.znp = znp;
    }


    public static void printResult(ZNP znp) {
        if (znp.isViolation()) {
            System.out.println(znp.getNumber() + " Создан: " + formatDateTime(znp.getDate()) +
                    " Должен быть завершен: " + formatDateTime(znp.getDeadline()) +
                    " Времени выделено: " + znp.getTotalTime() + " часа" +
                    " НАРУШЕНИЕ" +
                    " Изделия: " + znp.getList());
        } else {
            System.out.println(znp.getNumber() + " Создан: " + formatDateTime(znp.getDate()) +
                    " Должен быть завершен: " + formatDateTime(znp.getDeadline()) +
                    " Времени выделено: " + znp.getTotalTime() + " часа" +
                    " НОРМА" +
                    " Изделия: " + znp.getList());
        }


    }
    public static void printRatio() {
        System.out.printf("Нарушены сроки по %d из %d производств", znp.violation_count, main.znpList.size());
        System.out.println();

    }
}
