package it.asd.compliance.features.renewdocument;

import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record RenewDocumentCommand(
        @ValidUUID UUID documentId,
        @NotNull LocalDate newDataRilascio,
        @NotNull LocalDate newDataScadenza,
        String newNumero,
        String newFileUrl
) {
}
