package org.example;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class TimeCalc {
    @Getter
    public static int violation = 0;

    public boolean checkDate(LocalDateTime date, Period period) {
        LocalDateTime.now();
        LocalDateTime pastDate = switch (period) {
            case Year -> LocalDateTime.now().minusYears(1);
            case Month -> LocalDateTime.now().minusMonths(1);
            case Quarter -> LocalDateTime.now().minusMonths(3);
        };

        return !date.isBefore(pastDate);
    }

    public static LocalDateTime calculateWorkingDeadline(LocalDateTime start, long remainingMinutes) {
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

    public static boolean isWorkDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    public static void calculateTime(ZNP znp) {

        try {
            // Получаем общее время в минутах
            long totalMinutes = (long) (znp.getTotalTime() * 60);

            LocalDateTime start = znp.getDate();
            LocalDateTime deadline = calculateWorkingDeadline(start, totalMinutes);
            znp.setDeadline(deadline);

            if (LocalDateTime.now().isAfter(deadline)) {
                znp.setViolation(true);

                ZNP.violation_count++;
            } else {
                znp.setViolation(false);

            }
        } catch (Exception e) {
            System.err.println("Ошибка при расчете времени для " + znp.getNumber() + ": " + e.getMessage());
        }

    }
}
