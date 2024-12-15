package it.projects.translate.ui.controller;

import it.projects.translate.service.AudioRecognitionService;
import it.projects.translate.service.ExcelService;
import it.projects.translate.service.TranslationService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ConvertAudioController {

    private File selectedFolder;

    // Riferimento alla Label e al ProgressIndicator per mostrare il processo
    @FXML
    private Label selectedFolderLabel;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    public void onChooseFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");
        File folder = directoryChooser.showDialog(new Stage());

        if (folder != null) {
            selectedFolder = folder;
            System.out.println("Selected folder: " + folder.getAbsolutePath());

            // Aggiorna la label con il nome della cartella selezionata
            selectedFolderLabel.setText("Cartella selezionata: " + folder.getName());
        }
    }

    @FXML
    public void onGenerateExcel() throws IOException {
        if (selectedFolder == null) {
            System.out.println("No folder selected. Please choose a folder first.");
            return;
        }

        // Seleziona il file di output
        File file = chooseOutputFile();
        if (file == null) {
            System.out.println("No file selected for saving.");
            return;
        }

        // Mostra il ProgressIndicator
        progressIndicator.setVisible(true);

        // Avvia il task per la generazione dell'Excel
        startGenerationTask(file);
    }

    private File chooseOutputFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        return fileChooser.showSaveDialog(new Stage());
    }

    private void startGenerationTask(File file) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                return generateExcel(file);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                onSuccess();
            }

            @Override
            protected void failed() {
                super.failed();
                onFailure();
            }
        };

        // Avvia il task in un nuovo thread
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private Void generateExcel(File file) throws Exception {
        try {
            // Ottieni il percorso della risorsa
            File modelFile = loadModelFile();

            AudioRecognitionService audioRecognitionService = new AudioRecognitionService(modelFile.getAbsolutePath());
            TranslationService translationService = new TranslationService();

            List<String> fileNames = audioRecognitionService.getFileNames(selectedFolder.getAbsolutePath());
            List<String> recognizedTexts = audioRecognitionService.getRecognizedTexts(fileNames, selectedFolder.getAbsolutePath());
            List<String> translations = translationService.getTranslations(recognizedTexts);

            // Scrivi i dati nel file Excel
            ExcelService.writeTranslationsToExcel(fileNames, translations, file.getAbsolutePath());

        } catch (Exception e) {
            // Log degli errori
            System.err.println("Errore durante il processo di generazione Excel: " + e.getMessage());
            e.printStackTrace();
            throw e; // Rilancia l'eccezione per gestirla nel metodo failed()
        }
        return null;
    }

    private File loadModelFile() {
        ClassLoader classLoader = AudioRecognitionService.class.getClassLoader();
        File modelFile = new File(Objects.requireNonNull(classLoader.getResource("models/vosk-model-en-us-0.22-lgraph")).getFile());

        if (!modelFile.exists()) {
            throw new IllegalArgumentException("Modello non trovato: " + modelFile.getAbsolutePath());
        }
        return modelFile;
    }

    private void onSuccess() {
        progressIndicator.setVisible(false);
        System.out.println("Processo completato con successo.");
        showAlert(AlertType.INFORMATION, "Operazione completata", "Il file Excel è stato generato con successo.");
    }

    private void onFailure() {
        progressIndicator.setVisible(false);
        System.err.println("Errore durante il processo.");
        showAlert(AlertType.ERROR, "Errore durante l'operazione", "Si è verificato un errore durante la generazione del file Excel.");
    }

    // Metodo per mostrare un alert (modale) con immagine
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Carica l'immagine dal percorso delle risorse
        Image image = new Image(String.valueOf(getClass().getResource("/img/icon.jpg")));

        // Imposta l'immagine come icona della finestra dell'alert
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(image); // Aggiunge l'icona della finestra

        alert.showAndWait();
    }
}
