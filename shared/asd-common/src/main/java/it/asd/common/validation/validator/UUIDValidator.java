package it.asd.common.validation.validator;

import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.UUID;

public class UUIDValidator implements ConstraintValidator<ValidUUID, UUID> {

    @Override
    public boolean isValid(UUID value, ConstraintValidatorContext context) {
        return value != null && !value.equals(new UUID(0, 0));
    }
}
