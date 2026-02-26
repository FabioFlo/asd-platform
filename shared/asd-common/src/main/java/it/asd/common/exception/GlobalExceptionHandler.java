package it.asd.common.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleViolationException ex) {
        return ApiErrors.of(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ApiErrors.BUSINESS_RULE_VIOLATION,
                ex.getMessage(),
                Map.of("rule", ex.getRule()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        var fields = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return ApiErrors.of(
                HttpStatus.BAD_REQUEST,
                ApiErrors.VALIDATION_FAILED,
                "Validation failed",
                Map.of("fields", fields));
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ApiErrors.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrors.INTERNAL_ERROR,
                "An unexpected error occurred");
    }
}