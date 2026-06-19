package com.finup.exception;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleResponseStatus() {
        ResponseStatusException ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
        ResponseEntity<?> response = exceptionHandler.handleResponseStatus(ex);

        assertEquals(404, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(404, body.get("status"));
        assertEquals("Resource not found", body.get("message"));
        assertTrue(body.containsKey("timestamp"));
    }

    @Test
    void testHandleBadCredentials() {
        BadCredentialsException ex = new BadCredentialsException("Bad creds");
        ResponseEntity<?> response = exceptionHandler.handleBadCredentials(ex);

        assertEquals(401, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(401, body.get("status"));
        assertEquals("E-mail ou senha inválidos", body.get("message"));
    }

    @Test
    void testHandleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        FieldError fieldError = new FieldError("objectName", "field", "must not be null");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<?> response = exceptionHandler.handleValidation(ex);

        assertEquals(400, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(400, body.get("status"));
        assertEquals("field: must not be null", body.get("message"));
    }

    @Test
    void testHandleConstraintViolation() {
        ConstraintViolationException ex = new ConstraintViolationException("Violation", null);
        ResponseEntity<?> response = exceptionHandler.handleConstraintViolation(ex);

        assertEquals(400, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(400, body.get("status"));
        assertEquals("Violation", body.get("message"));
    }

    @Test
    void testHandleGeneral() {
        Exception ex = new Exception("General error");
        ResponseEntity<?> response = exceptionHandler.handleGeneral(ex);

        assertEquals(500, response.getStatusCodeValue());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(500, body.get("status"));
        assertEquals("Erro interno do servidor", body.get("message"));
    }
}
