package it.asd.scheduling.features.createvenue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateVenueCommand(
        @NotNull UUID asdId,
        @NotBlank String nome,
        String indirizzo,
        String citta,
        String provincia,
        String note
) {}
