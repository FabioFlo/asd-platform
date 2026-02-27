package it.asd.identity.features.addqualification;

import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.UUID;

public record AddQualificationCommand(
        @ValidUUID UUID personId,
        @NotBlank String tipo,
        @NotBlank String ente,
        @NotBlank String livello,
        LocalDate dataConseguimento,
        LocalDate dataScadenza,
        String numeroPatentino,
        String note
) {
}
