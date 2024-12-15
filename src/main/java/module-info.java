module convertToText {
    // Richiesta dei moduli necessari
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires org.json;
    requires org.slf4j;
    requires vosk;
    requires java.desktop;

    opens it.projects.translate.ui.controller to javafx.fxml;
    exports it.projects.translate.ui.controller;

    requires javafx.graphics;
    requires java.logging; // Necessario per JavaFX

    exports it.projects.translate.ui to javafx.graphics;
}


