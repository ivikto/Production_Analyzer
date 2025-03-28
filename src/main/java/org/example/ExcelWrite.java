package org.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class ExcelWrite {


    private static final String filePath = "C:\\Java\\Production_Analyzer\\prod_data.xlsx";


    public void createExcel(List<ZNP> znpList) {
        try {
            writeToExcel(znpList);
            log.info("Файл успешно создан.");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    // Метод для записи данных в Excel
    private void writeToExcel(List<ZNP> znpList) throws IOException {
        // Создаем новую книгу Excel
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Производства за месяц");

        // Создаем стиль для заголовков
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);


        // Стиль для дат
        CellStyle dateStyle = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd.MM.yyyy HH:mm"));
        dateStyle.setAlignment(HorizontalAlignment.CENTER);
        dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // Создаем строку с итогом
        long violations = znpList.stream()
                .filter(ZNP::isViolation)
                .count();
        String str = String.format("Нарушены сроки по %d из %d производств", violations, znpList.size());
        Row headerRow_summ = sheet.createRow(1);
        Cell firtsCell = headerRow_summ.createCell(0);
        firtsCell.setCellValue(str);


        sheet.addMergedRegion(new CellRangeAddress(
                0,  // Начальная строка (0-based)
                0,  // Конечная строка (та же)
                0,  // Начальная колонка (0 = A)
                6   // Конечная колонка (1 = B)
        ));


        // Создаем заголовки
        Row headerRow = sheet.createRow(1);
        String[] headers = {"№","Номер ЗНП", "Создан", "Должен быть завершен", "Нормочасов выделено", "Нарушение", "Номенклатура"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Заполняем данные
        int rowNum = 2;
        for (ZNP znp : znpList) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(rowNum - 1);
            row.createCell(1).setCellValue(znp.getNumber());
            Cell cell2 = row.createCell(2);
            cell2.setCellStyle(dateStyle);
            cell2.setCellValue(znp.getDate());
            Cell cell3 = row.createCell(3);
            cell3.setCellStyle(dateStyle);
            cell3.setCellValue(znp.getDeadline());
            row.createCell(4).setCellValue(znp.getTotalTime());
            row.createCell(5).setCellValue(znp.isViolation());
            row.createCell(6).setCellValue(znp.getList().toString());
        }

        // Авторазмер для колонок
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Записываем файл
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
        }

        // Закрываем книгу
        workbook.close();
    }
}
