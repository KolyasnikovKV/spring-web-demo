package ru.yandex.practicum.exception;

public class DataNotFoundException extends ServiceException {
    public DataNotFoundException(String message) {
        super(message);
    }
}
