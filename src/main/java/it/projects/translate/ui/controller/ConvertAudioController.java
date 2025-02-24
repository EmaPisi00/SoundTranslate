package it.projects.translate.ui.controller;

import it.projects.translate.service.AudioRecognitionService;
import it.projects.translate.service.ExcelService;
import it.projects.translate.service.TranslationService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
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
    private TextField folderPathInputField;

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

        File initialDirectory = new File(folderPathInputField.getText());
        if (initialDirectory.exists() && initialDirectory.isDirectory()) {
            directoryChooser.setInitialDirectory(initialDirectory);
        } else {
            directoryChooser.setInitialDirectory(selectedInputFolder);
        }

        File folder = directoryChooser.showDialog(new Stage());
        if (folder != null) {
            selectedInputFolder = folder;
            folderPathInputField.setText(folder.getAbsolutePath());
            selectedFolderInputLabel.setText("Cartella selezionata: " + folder.getName());
        }
    }

    @FXML
    public void onChooseFolderExcel() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder");

        File initialDirectory = new File(folderPathOutputField.getText());
        if (initialDirectory.exists() && initialDirectory.isDirectory()) {
            directoryChooser.setInitialDirectory(initialDirectory);
        } else {
            directoryChooser.setInitialDirectory(selectedOutputFolder);
        }

        File folder = directoryChooser.showDialog(new Stage());
        if (folder != null) {
            selectedOutputFolder = folder;
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

        File file = chooseOutputFile();

        startGenerationTask(file);
    }

    private File chooseOutputFile() {
        String folderPath = folderPathOutputField.getText();
        File outputDirectory = new File(folderPath);

        if (!outputDirectory.exists() || !outputDirectory.isDirectory()) {
            outputDirectory = selectedOutputFolder;
        }

        String filePath = folderPathOutputField.getText().trim();
        File file = new File(filePath);
        String fileName = file.getName();

        if (filePath.isEmpty() || !fileName.contains(".")) {
            fileName = "file_output_0.xlsx";
        }

        int startIndex = fileName.indexOf("file_output_") + "file_output_".length();
        int endIndex = fileName.indexOf(".");
        String number = fileName.substring(startIndex, endIndex).trim();

        int i = Integer.parseInt(number);
        File outputFile;
        do {
            i++; // Aggiungi 1 al numero
            fileName = "file_output_" + i + ".xlsx";
            outputFile = new File(outputDirectory, fileName);
        } while (outputFile.exists());

        return new File(outputDirectory, fileName);
    }

    private void startGenerationTask(File file) {
        Stage loadingStage = createLoadingStage(); // Crea uno stage separato per ogni esecuzione

        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                return generateExcel(file);
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                onSuccess(loadingStage);
            }

            @Override
            protected void failed() {
                super.failed();
                onFailure(loadingStage);
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        loadingStage.show();
        folderPathInputField.setText("");
        folderPathOutputField.setText("");
        thread.start();
    }

    private Stage createLoadingStage() {
        Stage loadingStage = new Stage();
        String inputLabel = selectedFolderInputLabel.getText();
        loadingStage.setTitle("Conversione file:  " + inputLabel.substring(inputLabel.indexOf(":") + 1).trim());

        Image image = new Image(String.valueOf(getClass().getResource("/img/icon.jpg")));
        loadingStage.getIcons().add(image);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        Label label = new Label("Attendere...");

        VBox vbox = new VBox(10, progressIndicator, label);
        vbox.setStyle("-fx-padding: 20px; -fx-alignment: center;");
        Scene scene = new Scene(vbox, 350, 250);
        loadingStage.setScene(scene);

        loadingStage.setAlwaysOnTop(true);
        loadingStage.setResizable(false);
        return loadingStage;
    }

    private Void generateExcel(File file) throws Exception {
        try {
            File modelFile = loadModelFile();
            AudioRecognitionService audioRecognitionService = new AudioRecognitionService(modelFile.getAbsolutePath());
            TranslationService translationService = new TranslationService();

            List<String> fileNames = audioRecognitionService.getFileNames(selectedInputFolder.getAbsolutePath());
            List<String> recognizedTexts = audioRecognitionService.getRecognizedTexts(fileNames, selectedInputFolder.getAbsolutePath());
            List<String> translations = translationService.getTranslations(recognizedTexts);

            ExcelService.writeTranslationsToExcel(fileNames, translations, file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Errore durante il processo di generazione Excel: " + e.getMessage());
            e.printStackTrace();
            throw e;
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

    private void onSuccess(Stage loadingStage) {
        System.out.println("Processo completato con successo.");
        showAlert(AlertType.INFORMATION, "Operazione completata", "Il file Excel è stato generato con successo.");
        loadingStage.close();
    }

    private void onFailure(Stage loadingStage) {
        System.err.println("Errore durante il processo.");
        showAlert(AlertType.ERROR, "Errore durante l'operazione", "Si è verificato un errore durante la generazione del file Excel.");
        loadingStage.close();
    }

    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Image image = new Image(String.valueOf(getClass().getResource("/img/icon.jpg")));
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        stage.getIcons().add(image);

        alert.showAndWait();
    }

    private void setupFolderPathListener(TextField textField, Label label, boolean isFolderPath) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            File file = new File(newValue);

            if (isFolderPath) {
                if (file.exists() && file.isDirectory()) {
                    label.setText("Cartella selezionata: " + file.getName());
                } else {
                    label.setText("Percorso non valido");
                }
            } else {
                if (!newValue.trim().isEmpty()) {
                    label.setText("File selezionato: " + file.getName());
                } else {
                    label.setText("Percorso non valido");
                }
            }
        });
    }
}
