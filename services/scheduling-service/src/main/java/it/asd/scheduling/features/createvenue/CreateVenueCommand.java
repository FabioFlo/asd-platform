package it.asd.scheduling.features.createvenue;

import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateVenueCommand(
        @ValidUUID UUID asdId,
        @NotBlank String nome,
        String indirizzo,
        String citta,
        String provincia,
        String note
) {
}
