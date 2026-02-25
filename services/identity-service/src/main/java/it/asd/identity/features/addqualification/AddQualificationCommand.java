package it.asd.identity.features.addqualification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AddQualificationCommand(
        @NotNull UUID personId,
        @NotBlank String tipo,
        @NotBlank String ente,
        @NotBlank String livello,
        LocalDate dataConseguimento,
        LocalDate dataScadenza,
        String numeroPatentino,
        String note
) {}
