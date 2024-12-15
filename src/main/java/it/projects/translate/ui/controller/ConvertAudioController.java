package it.projects.translate.ui.controller;

import it.projects.translate.service.AudioRecognitionService;
import it.projects.translate.service.ExcelService;
import it.projects.translate.service.TranslationService;
import it.projects.translate.utils.Constant;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class ConvertAudioController {

    private File selectedFolder;

    // Aggiungi il riferimento alla Label per visualizzare la cartella selezionata
    @FXML
    private Label selectedFolderLabel;

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

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {

            // Ottieni il percorso della risorsa
            ClassLoader classLoader = AudioRecognitionService.class.getClassLoader();
            File modelFile = new File(Objects.requireNonNull(classLoader.getResource(Constant.MODEL_PATH)).getFile());

            if (!modelFile.exists()) {
                throw new IllegalArgumentException("Modello non trovato: " + modelFile.getAbsolutePath());
            }

            AudioRecognitionService audioRecognitionService = new AudioRecognitionService(modelFile.getAbsolutePath());
            TranslationService translationService = new TranslationService();

            List<String> fileNames = audioRecognitionService.getFileNames(selectedFolder.getAbsolutePath());
            List<String> recognizedTexts = audioRecognitionService.getRecognizedTexts(fileNames, selectedFolder.getAbsolutePath());
            List<String> translations = translationService.getTranslations(recognizedTexts);

            ExcelService.writeTranslationsToExcel(fileNames, translations, file.getAbsolutePath());
        }
    }
}
