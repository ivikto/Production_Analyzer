package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.example.TimeCalc.formatDateTime;

@Slf4j
@Component
public class Output {

    public void printResult(ZNP znp) {
        double rounded = Math.round(znp.getTotalTime() * 1000.0) / 1000.0;
        String status;
        if (znp.isViolation()) {
            status = "НАРУШЕНИЕ";

        } else {
            status = "НОРМА";

        }
        String str = String.format(
                "%s Создан: %s Должен быть завершен: %s Времени выделено: %.1f часа %s Изделия: %s Posted: %s",
                znp.getNumber(),
                formatDateTime(znp.getDate()),
                formatDateTime(znp.getDeadline()),
                rounded,
                status,
                znp.getList(),
                znp.isPosted()
        );
        log.info(str);
    }
    public void printRatio(List<ZNP> znpList) {
        long violations = znpList.stream()
                .filter(ZNP::isViolation)
                .count();
        String str = String.format("Нарушены сроки по %d из %d производств", violations, znpList.size());
        log.info(str);


    }
}
