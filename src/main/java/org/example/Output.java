package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.example.TimeCalc.formatDateTime;

@Slf4j
@Component
public class Output {

    public static void printResult(ZNP znp) {
        double rounded = Math.round(znp.getTotalTime() * 1000.0) / 1000.0;
        if (znp.isViolation()) {
            String str = String.format(znp.getNumber() + " Создан: " + formatDateTime(znp.getDate()) +
                    " Должен быть завершен: " + formatDateTime(znp.getDeadline()) +
                    " Времени выделено: " + rounded + " часа" +
                    " НАРУШЕНИЕ" +
                    " Изделия: " + znp.getList());
            log.info(str);
        } else {
            String str = String.format(znp.getNumber() + " Создан: " + formatDateTime(znp.getDate()) +
                    " Должен быть завершен: " + formatDateTime(znp.getDeadline()) +
                    " Времени выделено: " + rounded + " часа" +
                    " НОРМА" +
                    " Изделия: " + znp.getList());
            log.info(str);
        }
    }
    public void printRatio(List<ZNP> znpList) {
        long violations = znpList.stream()
                .filter(ZNP::isViolation)
                .count();
        String str = String.format("Нарушены сроки по %d из %d производств", violations, znpList.size());
        log.info(str);


    }
}
