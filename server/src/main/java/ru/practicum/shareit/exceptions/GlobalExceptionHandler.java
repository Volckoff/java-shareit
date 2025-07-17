package ru.practicum.shareit.exceptions;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex) {
        log.error("Ресурс не найден: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Not found", "message", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateException(DuplicateException ex) {
        log.error("Ошибка дублирования данных: {}", ex.getMessage());
        return new ResponseEntity<>(Map.of("Error", "Resource already exists", "Message", ex.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Доступ запрещён: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Forbidden", "message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAllUncaughtException(Exception ex) {
        log.error("Непредвиденная ошибка: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error",
                        "message", "Произошла внутренняя ошибка сервера"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("Ошибка валидации параметров");
        log.warn("Ошибка валидации параметров: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Validation failed", "message", errorMessage));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> messages = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .toList();
        Map<String, Object> body = Map.of("error", "Validation failed", "messages", messages);
        log.error("Ошибка валидации: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequestException(BadRequestException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "BadRequestException Error");
        body.put("message", ex.getMessage());
        log.error("Ошибка в запросе: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CommentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleCommentNotValidException(CommentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        log.error("Ошибка коммента: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ValidationException.class)
    public Map<String, Object> handleValidationException(ValidationException ex) {
        Map<String, Object> fieldErrors = new HashMap<>();
        fieldErrors.put("timestamp", LocalDateTime.now());
        fieldErrors.put("status", HttpStatus.CONFLICT.value());
        fieldErrors.put("error", "Validation Error");
        fieldErrors.put("message", ex.getMessage());
        log.error("Ошибка валидации cущностей: {}", ex.getMessage());
        return fieldErrors;
    }

}
