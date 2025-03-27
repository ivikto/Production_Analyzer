package org.example;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Component
public class ZNP {
    private String ref_key;
    private String number;
    private Double totalTime;
    private LocalDateTime date;
    private LocalDateTime deadline;
    private List<String> list = new ArrayList<>();
    private boolean violation;
    public static int violation_count = 0;



    @Override
    public String toString() {
        return
                "Ref_Key='" + ref_key + '\'' +
                        ", Номер ЗнП='" + number + '\'' +
                        ", Общее время выполнения= " + totalTime +
                        ", Создан= " + date;
    }
}