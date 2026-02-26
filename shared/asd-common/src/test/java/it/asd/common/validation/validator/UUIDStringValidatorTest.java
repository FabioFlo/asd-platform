package it.asd.common.validation.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UUIDStringValidator")
class UUIDStringValidatorTest {

    private final UUIDStringValidator validator = new UUIDStringValidator();

    @Test
    @DisplayName("null is invalid")
    void nullIsInvalid() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @Test
    @DisplayName("blank string is invalid")
    void blankIsInvalid() {
        assertThat(validator.isValid("   ", null)).isFalse();
    }

    @Test
    @DisplayName("empty string is invalid")
    void emptyIsInvalid() {
        assertThat(validator.isValid("", null)).isFalse();
    }

    @Test
    @DisplayName("non-UUID string is invalid")
    void nonUUIDStringIsInvalid() {
        assertThat(validator.isValid("not-a-uuid", null)).isFalse();
    }

    @Test
    @DisplayName("zero UUID string (00000000-...) is invalid")
    void zeroUUIDStringIsInvalid() {
        assertThat(validator.isValid("00000000-0000-0000-0000-000000000000", null)).isFalse();
    }

    @Test
    @DisplayName("valid UUID string is valid")
    void validUUIDStringIsValid() {
        assertThat(validator.isValid("123e4567-e89b-12d3-a456-426614174000", null)).isTrue();
    }

    @Test
    @DisplayName("random UUID string is valid")
    void randomUUIDStringIsValid() {
        assertThat(validator.isValid(java.util.UUID.randomUUID().toString(), null)).isTrue();
    }
}
