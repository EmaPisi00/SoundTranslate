package it.projects.translate.service;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.util.List;

public class ExcelGenerationTaskService extends Task<Void> {
    private final File file;
    private final String selectInputFolder;
    private final Label percentLabel;
    private final Label progressLabel;

    public ExcelGenerationTaskService(File file, String selectInputFolder, Label percentLabel, Label progressLabel) {
        this.file = file;
        this.selectInputFolder = selectInputFolder;
        this.percentLabel = percentLabel;
        this.progressLabel = progressLabel;
    }

    @Override
    protected Void call() throws Exception {
        generateExcel(file);
        return null;
    }

    private void generateExcel(File file) throws Exception {
        try {
            // Fase 1: Carica il file modello
            File modelFile = loadModelFile();
            updateProgress(0.10, 1.0);
            updateLabel("10%", "2) Caricamento modello per la traduzione");

            // Fase 2: Servizio di riconoscimento audio
            AudioRecognitionService audioRecognitionService = new AudioRecognitionService(modelFile.getAbsolutePath());
            List<String> fileNames = audioRecognitionService.getFileNames(selectInputFolder);
            updateProgress(0.25, 1.0);
            updateLabel("25%","3) Lettura file audio");

            List<String> recognizedTexts = audioRecognitionService.getRecognizedTexts(fileNames, selectInputFolder);
            updateProgress(0.50, 1.0);
            updateLabel("50%", "4) Avvio processo di riconoscimento file audio");

            // Fase 3: Traduzione
            TranslationService translationService = new TranslationService();
            List<String> translations = translationService.getTranslations(recognizedTexts);
            updateProgress(0.75, 1.0);
            updateLabel("75%", "5) Avvio traduzioni");

            // Fase 4: Scrittura su Excel
            ExcelService.writeTranslationsToExcel(fileNames, translations, file.getAbsolutePath());
            updateProgress(1.0, 1.0);
            updateLabel("100%", "6) Fine, completato con successo!");

        } catch (Exception e) {
            System.err.println("Errore durante il processo di generazione Excel: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void updateLabel(String percentText, String progressText) {
        Platform.runLater(() -> {
            percentLabel.setText(percentText);
            progressLabel.setText(progressLabel.getText().concat("\n" + progressText));
        });
    }

    private File loadModelFile() {
        File modelFile = new File("vosk-model-en-us-0.22-lgraph");
        if (!modelFile.exists()) {
            throw new IllegalArgumentException("Modello non trovato: " + modelFile.getAbsolutePath());
        }
        return modelFile;
    }
}

