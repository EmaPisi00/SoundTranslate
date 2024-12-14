package it.projects.translate.service;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TranslationService {

    private static final Logger log = LoggerFactory.getLogger(TranslationService.class);

    public List<String> getTranslations(List<String> texts) {
        log.info("Avvio del processo di traduzione dei testi.");
        List<String> translations = new ArrayList<>();
        for (String text : texts) {
            log.info("Traduzione in corso per il testo: {}", text);
            String translation = translate(text);
            translations.add(translation);
            log.info("Testo tradotto: {}", translation);
        }
        return translations;
    }

    private String translate(String text) {
        try {
            JSONObject jsonObject = new JSONObject(text);
            String textToTranslate = jsonObject.getString("text");

            ProcessBuilder processBuilder = new ProcessBuilder("python", "data/script/translate.py", textToTranslate);
            processBuilder.environment().put("PYTHONIOENCODING", "utf-8");
            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            reader.close();
            log.info("Traduzione completata per il testo: {}", textToTranslate);

            String translation = output.toString().trim();

            log.info("Traduzione completata : {}", translation);

            return translation;
        } catch (IOException e) {
            log.info("Errore durante la traduzione: {}", e.getMessage());
            return "Errore nella traduzione.";
        }
    }
}
