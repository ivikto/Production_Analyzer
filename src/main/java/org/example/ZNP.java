package org.example;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ZNP {
    private String ref_key;
    private String number;
    private Double totalTime;
    private LocalDateTime date;
    private List<String> list = new ArrayList<>();



    @Override
    public String toString() {
        return
                "Ref_Key='" + ref_key + '\'' +
                        ", Номер ЗнП='" + number + '\'' +
                        ", Общее время выполнения= " + totalTime +
                        ", Создан= " + date;
    }
}