package it.asd.compliance.features.renewdocument;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Sealed result for RenewDocument.
 * Three cases the controller must handle exhaustively.
 */
public sealed interface RenewDocumentResult
        permits RenewDocumentResult.Renewed,
                RenewDocumentResult.NotFound,
                RenewDocumentResult.InvalidDateRange {

    record Renewed(
            UUID documentId,
            UUID personId,
            UUID asdId,
            String documentType,
            LocalDate newDataScadenza
    ) implements RenewDocumentResult {}

    record NotFound(UUID documentId) implements RenewDocumentResult {}

    record InvalidDateRange(String reason) implements RenewDocumentResult {}
}
