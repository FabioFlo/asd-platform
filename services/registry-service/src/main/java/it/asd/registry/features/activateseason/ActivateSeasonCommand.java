package it.asd.registry.features.activateseason;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ActivateSeasonCommand(
        @NotNull UUID asdId,
        @NotBlank String codice,
        @NotNull LocalDate dataInizio,
        @NotNull LocalDate dataFine
) {}
