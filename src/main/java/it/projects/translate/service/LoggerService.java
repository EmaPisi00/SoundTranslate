package it.projects.translate.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerService {

    private static String directoryFile;

    // Metodo che verifica se è la prima esecuzione
    public static void checkAndCreateLogFile() {
        // Crea la cartella "data" se non esiste
        File dataDirectory = new File("data");
        if (!dataDirectory.exists()) {
            boolean dirCreated = dataDirectory.mkdirs();  // Crea la cartella e tutte le sottocartelle necessarie
            if (!dirCreated) {
                System.err.println("Errore nella creazione della cartella 'data'");
                return;
            }
        }

        // Crea il file di log "log.txt" all'interno della cartella "data" con annesso orario
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("ddMMyyyy_HHmm");
        setDirectoryFile("data" + File.separator + dateTimeFormatter.format(now) + "_log.txt");
        File logFile = new File(dataDirectory, directoryFile);

        // Se il file esiste già, non fare nulla (appenderemo i log)
        if (!logFile.exists()) {
            try {
                boolean fileCreated = logFile.createNewFile();  // Crea il file
                if (!fileCreated) {
                    System.err.println("Errore nella creazione del file di log");
                }
            } catch (IOException e) {
                logToFile(e.getMessage());
                //e.printStackTrace();
            }
        }
    }

    // Metodo per scrivere le loggate nel file
    public static void logToFile(String logMessage) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(directoryFile, true))) {
            writer.write(logMessage);
            writer.newLine();  // Aggiunge una nuova riga dopo ogni messaggio
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(logMessage);
    }

    public static void setDirectoryFile(String directoryFile) {
        LoggerService.directoryFile = directoryFile;
    }

    public static String getDirectoryFile() {
        return directoryFile;
    }
}
