package org.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
public class OutputTest {

    @Autowired
    private Output output;

    @Test
    void testPrintResult() {
        ZNP znp = new ZNP();
        znp.setNumber("TEST-123");
        znp.setViolation(true);
        znp.setDate(LocalDateTime.now());
        znp.setDeadline(LocalDateTime.now().plusDays(1));
        znp.setTotalTime(10.5);
        znp.setList(List.of("Изделие1", "Изделие2"));

        assertDoesNotThrow(() -> output.printResult(znp));
    }

}
