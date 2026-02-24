package it.asd.compliance.features.uploaddocument;

import it.asd.compliance.shared.entity.DocumentEntity;
import java.time.LocalDate;
import java.util.UUID;

/**
 * HTTP response record. Static factory maps from entity.
 * MapStruct not needed here — flat one-to-one mapping.
 */
public record UploadDocumentResponse(
        UUID id,
        UUID personId,
        UUID asdId,
        String tipo,
        LocalDate dataRilascio,
        LocalDate dataScadenza,
        String stato
) {
    public static UploadDocumentResponse from(DocumentEntity e) {
        return new UploadDocumentResponse(
                e.getId(), e.getPersonId(), e.getAsdId(),
                e.getTipo().name(), e.getDataRilascio(),
                e.getDataScadenza(), e.getStato().name());
    }
}
