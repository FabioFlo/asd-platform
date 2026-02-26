package it.asd.common.validation.validator;

import it.asd.common.enums.AsdRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AsdRoleValidator")
class AsdRoleValidatorTest {

    private final AsdRoleValidator validator = new AsdRoleValidator();

    @Test
    @DisplayName("null is invalid")
    void nullIsInvalid() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @ParameterizedTest(name = "{0} is valid")
    @EnumSource(AsdRole.class)
    @DisplayName("all AsdRole values are valid")
    void allRolesAreValid(AsdRole role) {
        assertThat(validator.isValid(role, null)).isTrue();
    }
}
