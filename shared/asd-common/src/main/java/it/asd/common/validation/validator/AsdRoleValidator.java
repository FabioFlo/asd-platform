package it.asd.common.validation.validator;

import it.asd.common.enums.AsdRole;
import it.asd.common.validation.annotation.ValidRole;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class AsdRoleValidator implements ConstraintValidator<ValidRole, AsdRole> {

    private static final Set<AsdRole> VALID_ROLES = Set.of(AsdRole.values());

    @Override
    public boolean isValid(AsdRole role, ConstraintValidatorContext context) {
        return role != null && VALID_ROLES.contains(role);
    }
}
