package it.projects.translate.service;

import it.projects.translate.utils.Constant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LoggerService {

    // Metodo che verifica se è la prima esecuzione
    public static void checkAndCreateLogFile() {
        File logFile = new File(Constant.LOG_FILE_PATH);

        // Se il file esiste già, non fare nulla (appenderemo i log)
        if (!logFile.exists()) {
            // Se non esiste, creiamo una nuova istanza del file (e sovrascriviamo se esiste)
            try {
                logFile.createNewFile();  // Crea il file
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Se esiste, cancella il contenuto per il primo avvio dell'app
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(Constant.LOG_FILE_PATH, false))) {
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
