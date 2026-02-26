package it.asd.common.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Handles {@link ConstraintViolationException} thrown when {@code @Validated} is used
 * on controller method parameters or path variables. Complements {@link GlobalExceptionHandler}
 * which covers {@code @Valid @RequestBody} ({@code MethodArgumentNotValidException}).
 */
@RestControllerAdvice
@Order(1)
public class ValidatorExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        List<String> fields = ex.getConstraintViolations().stream()
                .map(v -> {
                    String field = StreamSupport
                            .stream(v.getPropertyPath().spliterator(), false)
                            .reduce((first, second) -> second)
                            .map(Path.Node::getName)
                            .orElse("unknown");
                    return field + ": " + v.getMessage();
                })
                .toList();
        return ApiErrors.of(
                HttpStatus.BAD_REQUEST,
                ApiErrors.VALIDATION_FAILED,
                "Validation failed",
                java.util.Map.of("fields", fields));
    }
}
