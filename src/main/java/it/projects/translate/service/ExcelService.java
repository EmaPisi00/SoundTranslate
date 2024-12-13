package it.projects.translate.service;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelService {

    public static void scriviTraduzioniInExcel(List<String> fileNames, List<String> traduzioni, String filePath) {
        Workbook workbook = new XSSFWorkbook();  // Creiamo un nuovo workbook Excel
        Sheet sheet = workbook.createSheet("Traduzioni");  // Creiamo un nuovo foglio chiamato "Traduzioni"

        // Creiamo la riga dell'intestazione
        Row headerRow = sheet.createRow(0);
        Cell cell1 = headerRow.createCell(0);
        cell1.setCellValue("NomeFileAudio");

        Cell cell2 = headerRow.createCell(1);
        cell2.setCellValue("Traduzione");

        // Aggiungiamo i dati sotto le intestazioni
        for (int i = 0; i < fileNames.size(); i++) {
            Row row = sheet.createRow(i + 1);
            Cell fileNameCell = row.createCell(0);
            fileNameCell.setCellValue(fileNames.get(i));

            Cell translationCell = row.createCell(1);
            translationCell.setCellValue(traduzioni.get(i));
        }

        // Scriviamo il file Excel su disco
        try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
            workbook.write(fileOut);  // Scriviamo il contenuto nel file
            workbook.close();  // Chiudiamo il workbook
            System.out.println("File Excel creato con successo: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}