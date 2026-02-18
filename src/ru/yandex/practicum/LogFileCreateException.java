package ru.yandex.practicum;

public class LogFileCreateException extends RuntimeException {

    public LogFileCreateException(String message, Throwable cause) {
        super(message, cause);
    }
}