package ru.yandex.practicum;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class WordleDictionaryLoader {

    private final PrintWriter log;

    public WordleDictionaryLoader(PrintWriter log) {
        this.log = log;
    }

    public WordleDictionary loadDictionary(String fileName) throws IOException {
        log.println("Загрузка словаря из файла: " + fileName);

        List<String> rawWords;
        try {
            rawWords = readFile(fileName);
        } catch (FileNotFoundException e) {
            throw new DictionaryNotFoundException("Файл словаря не найден: " + fileName, e);
        }
        log.println("Прочитано строк: " + rawWords.size());

        List<String> normalizedWords = normalizeAndFilter(rawWords);
        log.println("После фильтрации: " + normalizedWords.size() + " слов");

        if (normalizedWords.isEmpty()) {
            throw new DictionaryEmptyException("Нет подходящих слов из 5 букв");
        }

        return new WordleDictionary(normalizedWords, log);
    }

    private List<String> readFile(String fileName) throws IOException {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileName), "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        return lines;
    }

    private List<String> normalizeAndFilter(List<String> rawWords) {
        List<String> result = new ArrayList<>();

        for (String word : rawWords) {
            String normalized = normalizeWord(word);
            if (isValidWord(normalized)) {
                result.add(normalized);
            }
        }

        return result;
    }

    private String normalizeWord(String word) {
        String trimmed = word.trim();
        String lower = trimmed.toLowerCase();
        String withoutYo = lower.replace('ё', 'е');
        return withoutYo;
    }

    private boolean isValidWord(String word) {
        return word.matches("[а-я]{5}");
    }
}