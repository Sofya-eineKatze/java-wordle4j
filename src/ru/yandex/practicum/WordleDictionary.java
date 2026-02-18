package ru.yandex.practicum;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WordleDictionary {

    private final List<String> words;
    private final PrintWriter log;
    private final Random random = new Random();

    public WordleDictionary(List<String> words, PrintWriter log) {
        this.words = words;
        this.log = log;

        if (words.isEmpty()) {
            log.println("Словарь пуст!");
            throw new DictionaryEmptyException("Словарь не может быть пустым");
        }

        log.println("Создан словарь с " + words.size() + " словами");
    }

    public boolean contains(String word) {
        boolean result = words.contains(word);
        log.println("Проверка слова \"" + word + "\" в словаре: " + result);
        return result;
    }

    public String getRandomWord() {
        if (words.isEmpty()) {
            log.println("Словарь пуст");
            throw new DictionaryEmptyException("Словарь пуст");
        }

        int index = random.nextInt(words.size());
        String word = words.get(index);
        log.println("Выбрано случайное слово: " + word);
        return word;
    }

    public int size() {
        return words.size();
    }

    public List<String> getAllWords() {
        return new ArrayList<>(words);
    }
}