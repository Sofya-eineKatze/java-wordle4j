package ru.yandex.practicum;

public class DictionaryNotFoundException extends RuntimeException {

    public DictionaryNotFoundException(String message) {
        super(message);
    }

    public DictionaryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}