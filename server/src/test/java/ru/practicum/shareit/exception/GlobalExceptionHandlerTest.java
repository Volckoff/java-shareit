package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.practicum.shareit.exceptions.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleNotFoundException() {
        NotFoundException ex = new NotFoundException("not found");
        ResponseEntity<Map<String, String>> response = handler.handleNotFoundException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("error")).isEqualTo("Not found");
        assertThat(response.getBody().get("message")).isEqualTo("not found");
    }

    @Test
    void testHandleDuplicateException() {
        DuplicateException ex = new DuplicateException("duplicate");
        ResponseEntity<Map<String, String>> response = handler.handleDuplicateException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("Error")).isEqualTo("Resource already exists");
        assertThat(response.getBody().get("Message")).isEqualTo("duplicate");
    }

    @Test
    void testHandleAllUncaughtException() {
        Exception ex = new Exception("unexpected");
        ResponseEntity<Map<String, String>> response = handler.handleAllUncaughtException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("error")).isEqualTo("Internal server error");
        assertThat(response.getBody().get("message")).isEqualTo("Произошла внутренняя ошибка сервера");
    }

    @Test
    void testHandleConstraintViolationException() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("must not be null");
        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));
        ResponseEntity<Map<String, String>> response = handler.handleConstraintViolationException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("Validation failed");
        assertThat(response.getBody().get("message")).isEqualTo("must not be null");
    }


    @Test
    void testHandleCommentNotValidException() {
        CommentNotValidException ex = new CommentNotValidException("comment error");
        ResponseEntity<Map<String, Object>> response = handler.handleCommentNotValidException(ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("error")).isEqualTo("Bad Request");
        assertThat(response.getBody().get("message")).isEqualTo("comment error");
        assertThat(response.getBody().get("status")).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().get("timestamp")).isInstanceOf(LocalDateTime.class);
    }

    @Test
    void testHandleValidationException() {
        ValidationException ex = new ValidationException("validation error");
        Map<String, Object> response = handler.handleValidationException(ex);
        assertThat(response.get("error")).isEqualTo("Validation Error");
        assertThat(response.get("message")).isEqualTo("validation error");
        assertThat(response.get("status")).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.get("timestamp")).isInstanceOf(LocalDateTime.class);
    }
}
