package it.projects.translate.service;

import it.projects.translate.utils.Constant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LoggerService {

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

        // Crea il file di log "log.txt" all'interno della cartella "data"
        File logFile = new File(dataDirectory, "log.txt");

        // Se il file esiste già, non fare nulla (appenderemo i log)
        if (!logFile.exists()) {
            try {
                boolean fileCreated = logFile.createNewFile();  // Crea il file
                if (!fileCreated) {
                    System.err.println("Errore nella creazione del file di log");
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Se esiste, cancella il contenuto per il primo avvio dell'app
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, false))) {
                // Passare 'false' nel FileWriter significa che il file viene sovrascritto
                writer.write("");  // Sovrascrive il file con una stringa vuota
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Metodo per scrivere le loggate nel file
    public static void logToFile(String logMessage) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(Constant.LOG_FILE_PATH, true))) {
            writer.write(logMessage);
            writer.newLine();  // Aggiunge una nuova riga dopo ogni messaggio
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(logMessage);
    }
}
