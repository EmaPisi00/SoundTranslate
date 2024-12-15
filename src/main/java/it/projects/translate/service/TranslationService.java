package it.projects.translate.service;

import it.projects.translate.utils.Constant;
import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TranslationService {

    public List<String> getTranslations(List<String> texts) {
        LoggerService.logToFile("Avvio del processo di traduzione dei testi.");
        List<String> translations = new ArrayList<>();
        for (String text : texts) {
            LoggerService.logToFile("Traduzione in corso per il testo: " + text);
            String translation = translate(text);
            translations.add(translation);
            LoggerService.logToFile("Testo tradotto: " + translation);
        }
        return translations;
    }

    private String translate(String text) {
        try {
            JSONObject jsonObject = new JSONObject(text);
            String textToTranslate = jsonObject.getString("text");

            // Ottieni il percorso della risorsa
            ClassLoader classLoader = TranslationService.class.getClassLoader();
            File scriptFile = new File(Objects.requireNonNull(classLoader.getResource(Constant.SCRIPT_PYTHON_PATH)).getFile());

            if (!scriptFile.exists()) {
                throw new IllegalArgumentException("Script non trovato: " + scriptFile.getAbsolutePath());
            }

            ProcessBuilder processBuilder = new ProcessBuilder("python", scriptFile.getAbsolutePath(), textToTranslate);
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            reader.close();
            LoggerService.logToFile("Traduzione completata per il testo: " + textToTranslate);

            String translation = output.toString().trim();

            LoggerService.logToFile("Traduzione completata : " + translation);

            return translation;
        } catch (IOException e) {
            LoggerService.logToFile("Errore durante la traduzione: " + e.getMessage());
            return "Errore nella traduzione.";
        }
    }
}
