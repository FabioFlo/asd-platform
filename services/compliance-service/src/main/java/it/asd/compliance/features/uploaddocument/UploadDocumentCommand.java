package it.asd.compliance.features.uploaddocument;

import it.asd.compliance.shared.entity.DocumentType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Record = immutable command. No Lombok needed.
 * Bean Validation annotations work on record components.
 */
public record UploadDocumentCommand(
        @NotNull UUID personId,
        @NotNull UUID asdId,
        @NotNull DocumentType tipo,
        LocalDate dataRilascio,
        LocalDate dataScadenza,
        String numero,
        String enteRilascio,
        String fileUrl,
        String note
) {}
