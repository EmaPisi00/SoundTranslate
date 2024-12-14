package it.projects.translate.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class ConvertAudioMain extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Carica il file FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/convertAudio.fxml"));
        Parent root = loader.load();

        Image image = new Image(String.valueOf(getClass().getResource("/img/icon.jpg")));

        primaryStage.getIcons().add(image);
        // Imposta la scena e la finestra principale
        primaryStage.setTitle("Convert Audio Tool");
        primaryStage.setScene(new Scene(root, 800, 600)); // Dimensioni di default
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args); // Avvia l'applicazione JavaFX
    }
}
