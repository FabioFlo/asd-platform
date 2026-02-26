package it.asd.common.validation.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UUIDValidator")
class UUIDValidatorTest {

    private final UUIDValidator validator = new UUIDValidator();

    @Test
    @DisplayName("null is invalid")
    void nullIsInvalid() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @Test
    @DisplayName("zero UUID (00000000-...) is invalid")
    void zeroUUIDIsInvalid() {
        assertThat(validator.isValid(new UUID(0, 0), null)).isFalse();
    }

    @Test
    @DisplayName("random UUID is valid")
    void randomUUIDIsValid() {
        assertThat(validator.isValid(UUID.randomUUID(), null)).isTrue();
    }

    @Test
    @DisplayName("well-known non-zero UUID is valid")
    void wellKnownUUIDIsValid() {
        assertThat(validator.isValid(
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"), null)).isTrue();
    }
}
