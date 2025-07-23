package ru.practicum.shareit.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.practicum.shareit.exceptions.GatewayExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GatewayExceptionHandlerTest {

    private final GatewayExceptionHandler handler = new GatewayExceptionHandler();

    @Test
    void handleValidationException_shouldReturnBadRequestWithErrors() {
        // Мокаем BindingResult и FieldError
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("object", "name", "не должно быть пустым"),
                new FieldError("object", "description", "не должно быть пустым")
        ));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, String>> response = handler.handleValidationException(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsKey("error");
        String errorMsg = response.getBody().get("error");
        assertThat(errorMsg).contains("name: не должно быть пустым");
        assertThat(errorMsg).contains("description: не должно быть пустым");
    }

    @Test
    void handleConstraintViolation_shouldReturnBadRequestWithErrors() {
        // Мокаем ConstraintViolation и Path
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        when(path1.toString()).thenReturn("name");
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("не должно быть пустым");

        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path2 = mock(Path.class);
        when(path2.toString()).thenReturn("description");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("не должно быть пустым");

        ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation1, violation2));

        ResponseEntity<Map<String, String>> response = handler.handleConstraintViolation(ex);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).containsKey("error");
        String errorMsg = response.getBody().get("error");
        assertThat(errorMsg).contains("name: не должно быть пустым");
        assertThat(errorMsg).contains("description: не должно быть пустым");
    }

    @Test
    void handleIllegalArgument_shouldReturnBadRequestWithErrorMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("Unknown state: UNSUPPORTED");

        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgument(ex);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(response.getBody()).containsEntry("error", "Unknown state: UNSUPPORTED");
    }
}