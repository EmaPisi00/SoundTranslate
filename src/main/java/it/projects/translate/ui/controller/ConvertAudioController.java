package it.projects.translate.ui.controller;

import it.projects.translate.service.ExcelGenerationTaskService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

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
        Stage loadingStage = createLoadingStage();
        VBox vbox = (VBox) loadingStage.getScene().getRoot();

        Label loadingLabel = (Label) vbox.getChildren().get(1);
        Label percentLabel = (Label) vbox.getChildren().get(2);
        ProgressBar progressBar = (ProgressBar) vbox.getChildren().get(3);
        Label progressLabel = (Label) vbox.getChildren().get(4);
        Button okButton = (Button) vbox.getChildren().get(5);


        ExcelGenerationTaskService task = new ExcelGenerationTaskService(file, selectedInputFolder.getAbsolutePath(), percentLabel, progressLabel);

        // Collega la progressProperty della Task alla ProgressBar
        progressBar.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(event -> {
            onSuccess(loadingStage, percentLabel, loadingLabel, okButton);
        });

        task.setOnFailed(event -> {
            onFailure(loadingStage);
        });

        Thread thread = new Thread(task);
        folderPathInputField.setText("");
        folderPathOutputField.setText("");
        thread.setDaemon(true);
        loadingStage.show();
        thread.start();
    }

    private Stage createLoadingStage() {
        Stage loadingStage = new Stage();
        String inputLabel = selectedFolderInputLabel.getText();
        loadingStage.setTitle("Traduzione");

        Image image = new Image(String.valueOf(getClass().getResource("/img/icon.jpg")));
        loadingStage.getIcons().add(image);

        // Titolo Stage
        Label titleStage = new Label("Traduzione personaggio:  " +  inputLabel.substring(inputLabel.indexOf(":") + 1).trim());
        titleStage.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-font-family: 'Times New Roman'");


        // Percentuale di progresso
        Label percentLabel = new Label("0%");
        percentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");

        // ProgressBar personalizzata
        ProgressBar progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);
        progressBar.setStyle("-fx-accent: #4CAF50;"); // Cambia colore della barra
        progressBar.setEffect(new DropShadow(5, Color.GRAY)); // Aggiunge ombra

        // Puntini di caricamento animati
        Label loadingLabel = new Label("Attendere...");
        loadingLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

        // Label di avanzamento
        Label progressLabel = new Label("1) Inizializzazione...");

        // Button OK (inizialmente nascosto)
        Button okButton = new Button("OK");
        okButton.setFocusTraversable(false);
        okButton.setVisible(false); // Il bottone OK è nascosto fino al completamento

        // Layout
        VBox vbox = new VBox(10,titleStage, loadingLabel, percentLabel, progressBar, progressLabel, okButton);
        vbox.setStyle("-fx-padding: 20px; -fx-alignment: center;");
        Scene scene = new Scene(vbox, 330, 300);
        loadingStage.setScene(scene);

        loadingStage.setAlwaysOnTop(true);
        loadingStage.setResizable(false);
        return loadingStage;
    }

    private void onSuccess(Stage loadingStage, Label percentLabel, Label loadingLabel, Button okButton) {
        System.out.println("Generazione Excel completata.");

        // Cambia il testo della percentuale
        Platform.runLater(() -> {
            percentLabel.setText("100%");
            loadingLabel.setText("Completato!");
        });

        // Rendi visibile il bottone OK
        Platform.runLater(() -> {
            okButton.setVisible(true);
            okButton.setOnAction(event -> loadingStage.close());
        });
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
