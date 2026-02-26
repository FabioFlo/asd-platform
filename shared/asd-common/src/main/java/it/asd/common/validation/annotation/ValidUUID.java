package it.asd.common.validation.annotation;

import it.asd.common.validation.ValidationMessages;
import it.asd.common.validation.validator.UUIDStringValidator;
import it.asd.common.validation.validator.UUIDValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {UUIDValidator.class, UUIDStringValidator.class})
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUUID {

    String message() default ValidationMessages.UUID_MUST_BE_VALID;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
