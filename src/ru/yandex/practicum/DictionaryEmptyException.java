package ru.yandex.practicum;

public class DictionaryEmptyException extends RuntimeException {

    public DictionaryEmptyException(String message) {
        super(message);
    }
}