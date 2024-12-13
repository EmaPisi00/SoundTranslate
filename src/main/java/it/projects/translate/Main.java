package it.projects.translate;

import it.projects.translate.service.ExcelService;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        // Percorsi come nel tuo codice
        String modelPath = "data/models/vosk-model-en-us-0.42-gigaspeech";
        String audioDirectoryPath = "C:\\Users\\emanu\\Desktop\\test";
        String outputFilePath = "C:\\Users\\emanu\\Desktop\\output.xlsx";  // Output in formato Excel

        try {
            Model model = new Model(modelPath);

            // Crea le liste per i nomi dei file e le traduzioni
            List<String> fileNames = new ArrayList<>();
            List<String> traduzioni = new ArrayList<>();

            // Ottieni la lista dei file audio nella cartella
            File folder = new File(audioDirectoryPath);
            File[] files = folder.listFiles((dir, name) -> name.endsWith(".wav"));

            if (files != null) {
                // Ciclo su ogni file audio
                for (File audioFile : files) {
                    String fileName = audioFile.getName();
                    System.out.println("Processando file: " + fileName);

                    // Esegui il riconoscimento dell'audio per estrarre il testo
                    String riconosciuto = riconosciTestoDaAudio(audioFile, model);
                    System.out.println("Testo riconosciuto: " + riconosciuto);

                    // Traduci il testo riconosciuto
                    String tradotto = traduci(riconosciuto);
                    System.out.println("Testo tradotto: " + tradotto);

                    // Aggiungi il nome del file e la traduzione nelle liste
                    fileNames.add(fileName);
                    traduzioni.add(tradotto);
                }

                // Scrivi i dati nel file Excel
                ExcelService.scriviTraduzioniInExcel(fileNames, traduzioni, outputFilePath);
            } else {
                System.out.println("La cartella non contiene file .wav.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Metodo per il riconoscimento del testo da un file audio .wav
    private static String riconosciTestoDaAudio(File audioFile, Model model) {
        try {
            // Apre il file audio .wav e crea un BufferedInputStream
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(audioFile));
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);

            // Ottieni il formato audio
            AudioFormat format = audioInputStream.getFormat();
            byte[] buffer = new byte[4096];  // Puoi regolare questa dimensione a seconda delle tue necessità

            // Crea il riconoscitore Vosk
            Recognizer recognizer = new Recognizer(model, format.getSampleRate());

            // Riconoscimento
            while (audioInputStream.read(buffer) > 0) {
                if (recognizer.acceptWaveForm(buffer, buffer.length)) {
                    // Se la parte del file audio è stata riconosciuta, stampa il risultato
                    recognizer.getResult();
                }
            }

            // Gestisci il risultato finale (quando l'audio è finito)
            return recognizer.getFinalResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // Metodo per tradurre il testo usando lo script Python
    private static String traduci(String text) {
        try {
            // Costruisci il comando per eseguire lo script Python
            ProcessBuilder processBuilder = new ProcessBuilder("python", "data/script/translate.py", text);
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            Process process = processBuilder.start();

            // Leggi l'output dello script Python
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            String line;
            StringBuilder output = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            // Chiudi il reader
            reader.close();

            // Estrai la parte dopo "Testo tradotto: "
            String finalOutput = output.toString();
            String translatedText = "";

            if (finalOutput.contains("Testo tradotto:")) {
                // Separare la parte dopo i due punti
                translatedText = finalOutput.split("Testo tradotto:")[1].trim();
            }

            // Restituisce la traduzione
            return translatedText;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
