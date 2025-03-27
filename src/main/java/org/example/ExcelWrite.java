package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class ExcelWrite {


    private static String filePath = "C:\\Java\\Production_Analyzer\\prod_data.xlsx";


    public static void createExcel(List<ZNP> znpList) {
        try {
            writeToExcel(znpList, filePath);
            System.out.println("Файл успешно создан.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Метод для записи данных в Excel
    private static void writeToExcel(List<ZNP> znpList, String fileName) throws IOException {
        // Создаем новую книгу Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Производства за месяц");

        // Создаем стиль для заголовков
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Создаем заголовки
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Номер ЗНП", "Создан", "Должен быть завершен", "Нормочасов выделено", "Нарушение", "Номенклатура"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Заполняем данные
        int rowNum = 1;
        for (ZNP znp : znpList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(znp.getNumber());
            row.createCell(1).setCellValue(znp.getDate());
            row.createCell(2).setCellValue(znp.getDeadline());
            row.createCell(3).setCellValue(znp.getTotalTime());
            row.createCell(4).setCellValue(znp.isViolation());
            row.createCell(5).setCellValue(znp.getList().toString());
        }

        // Авторазмер для колонок
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Записываем файл
        try (FileOutputStream fileOut = new FileOutputStream(fileName)) {
            workbook.write(fileOut);
        }

        // Закрываем книгу
        workbook.close();
    }
}
