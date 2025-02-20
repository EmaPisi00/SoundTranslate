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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConvertAudioController {

    @FXML
    private Label selectedFolderInputLabel;

    @FXML
    private Label selectedFolderOutputLabel;

    @FXML
    private ProgressIndicator progressIndicator;

    @FXML
    private TextField folderPathInputField; // TextField dove l'utente può incollare il percorso

    @FXML
    private TextField folderPathOutputField;

    private File selectedInputFolder = null;
    private File selectedOutputFolder = null;

    @FXML
    public void initialize() {
        setupFolderPathListener(folderPathInputField, selectedFolderInputLabel, true);
        setupFolderPathListener(folderPathOutputField, selectedFolderOutputLabel, false);
    }

    @FXML
    public void onChooseFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");

        // Se il campo di testo ha un percorso valido, usalo come cartella iniziale
        File initialDirectory = new File(folderPathInputField.getText());
        if (initialDirectory.exists() && initialDirectory.isDirectory()) {
            directoryChooser.setInitialDirectory(initialDirectory);
        } else {
            directoryChooser.setInitialDirectory(selectedInputFolder); // Usa la cartella predefinita
        }

        File folder = directoryChooser.showDialog(new Stage());

        if (folder != null) {
            selectedInputFolder = folder;
            System.out.println("Selected folder: " + folder.getAbsolutePath());

            // Aggiorna il campo di testo e la label con il percorso selezionato
            folderPathInputField.setText(folder.getAbsolutePath());
            selectedFolderInputLabel.setText("Cartella selezionata: " + folder.getName());
        }
    }

    @FXML
    public void onChooseFolderExcel() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");

        // Se il campo di testo ha un percorso valido, usalo come cartella iniziale
        File initialDirectory = new File(folderPathOutputField.getText());
        if (initialDirectory.exists() && initialDirectory.isDirectory()) {
            directoryChooser.setInitialDirectory(initialDirectory);
        } else {
            directoryChooser.setInitialDirectory(selectedOutputFolder); // Usa la cartella predefinita
        }

        File folder = directoryChooser.showDialog(new Stage());

        if (folder != null) {
            selectedOutputFolder = folder;
            System.out.println("Selected folder: " + folder.getAbsolutePath());

            // Aggiorna il campo di testo e la label con il percorso selezionato
            folderPathOutputField.setText(folder.getAbsolutePath());
            selectedFolderOutputLabel.setText("Cartella selezionata: " + folder.getName());
        }
    }

    @FXML
    public void onGenerateExcel() throws IOException {
        if (selectedInputFolder == null || selectedOutputFolder == null) {
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
        String folderPath = folderPathOutputField.getText();
        File outputDirectory = new File(folderPath);

        // Verifica se la cartella esiste, altrimenti usa la cartella predefinita
        if (!outputDirectory.exists() || !outputDirectory.isDirectory()) {
            outputDirectory = selectedOutputFolder;
        }

        // Ottieni il nome del file dall'input dell'utente
        String filePath = folderPathOutputField.getText().trim(); // Ottieni il percorso dal TextField
        File file = new File(filePath);

        String fileName = file.getName(); // Estrai solo il nome del file

        // Se il percorso è vuoto o non contiene un file valido, assegna un nome predefinito
        if (filePath.isEmpty() || !fileName.contains(".")) {
            fileName = "file_output.xlsx";
        }

        System.out.println("Nome del file: " + fileName);


        // Crea il file nella cartella selezionata
        return new File(outputDirectory, fileName);
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

            List<String> fileNames = audioRecognitionService.getFileNames(selectedInputFolder.getAbsolutePath());
            List<String> recognizedTexts = audioRecognitionService.getRecognizedTexts(fileNames, selectedInputFolder.getAbsolutePath());
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
        File modelFile = new File("vosk-model-en-us-0.22-lgraph");
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

    // Metodo per centralizzare la logica del listener e aggiornare le label di input e output
    private void setupFolderPathListener(TextField textField, Label label, boolean isFolderPath) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            File file = new File(newValue);

            if (isFolderPath) { // Se deve essere una cartella valida
                if (file.exists() && file.isDirectory()) {
                    label.setText("Cartella selezionata: " + file.getName());
                } else {
                    label.setText("Percorso non valido");
                }
            } else { // Se deve essere un file (anche inesistente)
                if (!newValue.trim().isEmpty()) {
                    label.setText("File selezionato: " + file.getName());
                } else {
                    label.setText("Percorso non valido");
                }
            }
        });
    }

}
