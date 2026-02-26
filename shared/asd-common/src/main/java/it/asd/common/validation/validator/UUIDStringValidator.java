package it.asd.common.validation.validator;

import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.UUID;

public class UUIDStringValidator implements ConstraintValidator<ValidUUID, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return false;
        try {
            UUID parsed = UUID.fromString(value);
            return !parsed.equals(new UUID(0, 0));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
