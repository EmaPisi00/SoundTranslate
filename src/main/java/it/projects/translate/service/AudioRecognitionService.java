package it.projects.translate.service;

import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AudioRecognitionService {

    private final Model model;

    public AudioRecognitionService(String modelPath) throws IOException {
        LoggerService.checkAndCreateLogFile();
        LoggerService.logToFile("Caricamento del modello Vosk da: " + modelPath);
        this.model = new Model(modelPath);
    }

    public List<String> getFileNames(String directoryPath) {
        LoggerService.logToFile("Lettura dei file audio dalla directory: " + directoryPath);
        File folder = new File(directoryPath);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".wav"));

        List<String> fileNames = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                fileNames.add(file.getName());
                LoggerService.logToFile("Trovato file audio: " + file.getName());
            }
        } else {
            LoggerService.logToFile("Nessun file .wav trovato nella directory specificata.");
        }
        return fileNames;
    }

    public List<String> getRecognizedTexts(List<String> fileNames, String directoryPath) {
        LoggerService.logToFile("Avvio del processo di riconoscimento del testo dai file audio.");
        List<String> recognizedTexts = new ArrayList<>();
        for (String fileName : fileNames) {
            File audioFile = new File(directoryPath + "/" + fileName);
            LoggerService.logToFile("Processando il file: " + fileName);
            String recognizedText = recognizeAudio(audioFile);
            recognizedTexts.add(recognizedText);
            LoggerService.logToFile("Testo riconosciuto per " + fileName + ": " + recognizedText);
        }
        return recognizedTexts;
    }

    private String recognizeAudio(File audioFile) {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(audioFile));
             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream)) {

            AudioFormat format = audioInputStream.getFormat();
            byte[] buffer = new byte[4096];
            Recognizer recognizer = new Recognizer(model, format.getSampleRate());

            while (audioInputStream.read(buffer) > 0) {
                recognizer.acceptWaveForm(buffer, buffer.length);
            }

            String finalResult = recognizer.getFinalResult();
            LoggerService.logToFile("Risultato finale ottenuto dal riconoscimento audio.");
            return finalResult;
        } catch (Exception e) {
            LoggerService.logToFile("Errore durante il riconoscimento audio: " + e.getMessage());
            return "";
        }
    }
}
