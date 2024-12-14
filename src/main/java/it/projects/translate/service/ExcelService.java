package it.projects.translate.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelService {

    public static void writeTranslationsToExcel(List<String> fileNames, List<String> translations, String filePath) {
        LoggerService.logToFile("Creazione del file Excel in corso: " + filePath);
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Traduzioni");

        // Creazione dell'intestazione
        Row headerRow = sheet.createRow(0);
        createHeaderCell(headerRow, 0, "Nome File Audio", workbook);
        createHeaderCell(headerRow, 1, "Traduzione", workbook);

        // Scrittura dei dati
        for (int i = 0; i < fileNames.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(fileNames.get(i));
            row.createCell(1).setCellValue(translations.get(i));
            LoggerService.logToFile("Scritti dati per il file: " + fileNames.get(i));
        }

        // Autosize per le colonne
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        // Scrittura su disco
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);
            LoggerService.logToFile("File Excel creato con successo: " + filePath);
        } catch (IOException e) {
            LoggerService.logToFile("Errore durante la scrittura del file Excel: " + e.getMessage());
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                LoggerService.logToFile("Errore durante la chiusura del workbook: " + e.getMessage());
            }
        }
    }

    private static void createHeaderCell(Row row, int column, String text, Workbook workbook) {
        Cell cell = row.createCell(column);
        cell.setCellValue(text);

        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        cell.setCellStyle(style);

        LoggerService.logToFile("Creata cella di intestazione per colonna: " + text);
    }
}
