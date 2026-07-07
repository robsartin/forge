package com.robsartin.graphs.application;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler for consistent error responses across all controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        logger.warn("Bad request: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "BAD_REQUEST",
                ex.getMessage(),
                Instant.now().toString(),
                request.getRequestURI(),
                MDC.get("correlationId")
        ));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        logger.warn("Entity not found: {} - Path: {}", ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                "NOT_FOUND",
                ex.getMessage(),
                Instant.now().toString(),
                request.getRequestURI(),
                MDC.get("correlationId")
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        logger.warn("Validation failed: {} - Path: {}", errors, request.getRequestURI());
        return ResponseEntity.badRequest().body(new ErrorResponse(
                "VALIDATION_ERROR",
                errors,
                Instant.now().toString(),
                request.getRequestURI(),
                MDC.get("correlationId")
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        logger.error("Unexpected error: {} - Path: {}", ex.getMessage(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                Instant.now().toString(),
                request.getRequestURI(),
                MDC.get("correlationId")
        ));
    }

    /**
     * Structured error response.
     */
    public record ErrorResponse(
            String error,
            String message,
            String timestamp,
            String path,
            String correlationId
    ) {}
}
