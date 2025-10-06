package ru.yandex.practicum.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.yandex.practicum.exception.DataNotFoundException;

@ControllerAdvice
@Slf4j
public class ServiceControllerAdvice {

    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<String> handleException(DataNotFoundException e) {
        log.info(e.getMessage());
        return ResponseEntity.notFound().build();
    }
}
