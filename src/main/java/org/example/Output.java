package org.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.example.TimeCalc.formatDateTime;

@Slf4j
@Component
public class Output {

    public void printResult(ZNP znp) {
        log.info(getResult(znp));
    }

    public String getResult(ZNP znp) {
        return String.format(
                "%s Создан: %s Должен быть завершен: %s Времени выделено: %.1f часа %s Изделия: %s Posted: %s",
                znp.getNumber(),
                formatDateTime(znp.getDate()),
                formatDateTime(znp.getDeadline()),
                getRoundedTime(znp.getTotalTime()),
                getStatus(znp.isViolation()),
                znp.getList(),
                znp.isPosted()
        );
    }

    public void printRatio(List<ZNP> znpList) {
        log.info(getRatioMessage(znpList));
    }


    public String getRatioMessage(List<ZNP> znpList) {
        long violations = countViolations(znpList);
        return String.format("Нарушены сроки по %d из %d производств", violations, znpList.size());
    }

    private long countViolations(List<ZNP> znpList) {
        return znpList.stream()
                .filter(ZNP::isViolation)
                .count();
    }

    private double getRoundedTime(double totalTime) {
        return Math.round(totalTime * 10.0) / 10.0; // Округление до 1 знака после запятой
    }

    private String getStatus(boolean isViolation) {
        return isViolation ? "НАРУШЕНИЕ" : "НОРМА";
    }
}