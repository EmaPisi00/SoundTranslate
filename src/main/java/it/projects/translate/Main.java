package it.projects.translate;

import it.projects.translate.service.AudioRecognitionService;
import it.projects.translate.service.ExcelService;
import it.projects.translate.service.TranslationService;
import it.projects.translate.utils.Constant;

import java.io.IOException;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        AudioRecognitionService audioRecognitionService = new AudioRecognitionService(Constant.MODEL_PATH);
        TranslationService translationService = new TranslationService();

        List<String> fileNames = audioRecognitionService.getFileNames(Constant.AUDIO_DIRECTORY_PATH);
        List<String> recognizedTexts = audioRecognitionService.getRecognizedTexts(fileNames, Constant.AUDIO_DIRECTORY_PATH);
        List<String> translations = translationService.getTranslations(recognizedTexts);

        ExcelService.writeTranslationsToExcel(fileNames, translations, Constant.OUTPUT_FILE_PATH);
    }
}
