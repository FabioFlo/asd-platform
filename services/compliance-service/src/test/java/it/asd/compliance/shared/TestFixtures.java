package it.asd.compliance.shared;

import it.asd.compliance.features.uploaddocument.UploadDocumentCommand;
import it.asd.compliance.shared.entity.DocumentEntity;
import it.asd.compliance.shared.entity.DocumentStatus;
import it.asd.compliance.shared.entity.DocumentType;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Centralized test data for compliance-service.
 * All test classes import from here — never build entities inline.
 */
public final class TestFixtures {

    private TestFixtures() {}

    public static final UUID PERSON_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID ASD_ID    = UUID.fromString("22222222-2222-2222-2222-222222222222");

    // ── Commands ──────────────────────────────────────────────────────────────

    public static UploadDocumentCommand validUploadDocumentCommand() {
        return new UploadDocumentCommand(
                PERSON_ID,
                ASD_ID,
                DocumentType.CERTIFICATO_MEDICO_AGONISTICO,
                LocalDate.now().minusDays(10),
                LocalDate.now().plusYears(1),
                "DOC-001",
                "ASL Roma",
                null,
                null
        );
    }

    public static UploadDocumentCommand commandWithInvalidDateRange() {
        return new UploadDocumentCommand(
                PERSON_ID,
                ASD_ID,
                DocumentType.CERTIFICATO_MEDICO_AGONISTICO,
                LocalDate.now().plusDays(10),   // rilascio AFTER scadenza
                LocalDate.now(),                // scadenza BEFORE rilascio
                null, null, null, null
        );
    }

    // ── Entities (for repository stubs in unit tests) ─────────────────────────

    public static DocumentEntity validDocument(UUID id) {
        return DocumentEntity.builder()
                .id(id)
                .personId(PERSON_ID)
                .asdId(ASD_ID)
                .tipo(DocumentType.CERTIFICATO_MEDICO_AGONISTICO)
                .dataRilascio(LocalDate.now().minusDays(10))
                .dataScadenza(LocalDate.now().plusYears(1))
                .stato(DocumentStatus.VALID)
                .build();
    }

    public static DocumentEntity validDocument() {
        return validDocument(UUID.randomUUID());
    }

    public static DocumentEntity expiredDocument(UUID id) {
        return DocumentEntity.builder()
                .id(id)
                .personId(PERSON_ID)
                .asdId(ASD_ID)
                .tipo(DocumentType.CERTIFICATO_MEDICO_AGONISTICO)
                .dataRilascio(LocalDate.now().minusYears(2))
                .dataScadenza(LocalDate.now().minusDays(1))
                .stato(DocumentStatus.VALID)
                .build();
    }

    public static DocumentEntity expiringSoonDocument(UUID id) {
        return DocumentEntity.builder()
                .id(id)
                .personId(PERSON_ID)
                .asdId(ASD_ID)
                .tipo(DocumentType.CERTIFICATO_MEDICO_AGONISTICO)
                .dataRilascio(LocalDate.now().minusYears(1))
                .dataScadenza(LocalDate.now().plusDays(15))   // within 30-day warning window
                .stato(DocumentStatus.VALID)
                .build();
    }
}
