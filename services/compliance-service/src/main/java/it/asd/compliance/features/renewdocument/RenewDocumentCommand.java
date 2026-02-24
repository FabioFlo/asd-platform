package it.asd.compliance.features.renewdocument;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record RenewDocumentCommand(
        @NotNull UUID documentId,
        @NotNull LocalDate newDataRilascio,
        @NotNull LocalDate newDataScadenza,
        String   newNumero,
        String   newFileUrl
) {}
