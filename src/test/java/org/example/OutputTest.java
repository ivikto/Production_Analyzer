package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
public class OutputTest {
    public static List<ZNP> znpList = new ArrayList<>();
    static {
        for (int i = 0; i < 10; i++) {
            ZNP znp = new ZNP();
            znp.setNumber("TEST" + i);
            znp.setViolation(false);
            znp.setDate(LocalDateTime.now());
            znp.setDeadline(LocalDateTime.now().plusDays(1));
            znp.setTotalTime(15d);
            znp.setList(List.of("Изделие1" + i, "Изделие2" + 2));
            znpList.add(znp);
        }
    }

    @Autowired
    private Output output;

    @Test
    void testPrintResult() {
        znpList.forEach(znp ->
                assertDoesNotThrow(() -> output.printResult(znp))
        );
    }

    @Test
    void testPrintRatio() {

        assertDoesNotThrow(() -> output.printRatio(znpList));
    }

}
