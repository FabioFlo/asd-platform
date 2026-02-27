package it.asd.compliance.features.uploaddocument;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Sealed interface — the handler can return exactly two outcomes.
 * The compiler enforces exhaustive handling at every call site.
 * No exceptions thrown for control flow.
 */
public sealed interface UploadDocumentResult
        permits UploadDocumentResult.Success,
        UploadDocumentResult.InvalidDateRange {

    /**
     * Document was persisted and the creation event was published.
     */
    record Success(
            UUID documentId,
            UUID personId,
            UUID asdId,
            String documentType,
            LocalDate dataScadenza
    ) implements UploadDocumentResult {
    }

    /**
     * dataScadenza is before dataRilascio — business rule violation.
     */
    record InvalidDateRange(
            LocalDate dataRilascio,
            LocalDate dataScadenza,
            String reason
    ) implements UploadDocumentResult {
    }
}
