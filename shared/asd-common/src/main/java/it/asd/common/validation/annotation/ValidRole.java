package it.asd.common.validation.annotation;

import it.asd.common.validation.ValidationMessages;
import it.asd.common.validation.validator.AsdRoleValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AsdRoleValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRole {

    String message() default ValidationMessages.ROLE_MUST_BE_VALID;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
