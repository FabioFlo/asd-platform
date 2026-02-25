package it.asd.compliance.features.uploaddocument;

import it.asd.common.kafka.EventPublisher;
import it.asd.compliance.shared.entity.DocumentEntity;
import it.asd.compliance.shared.entity.DocumentStatus;
import it.asd.compliance.shared.repository.DocumentRepository;
import it.asd.events.KafkaTopics;
import it.asd.events.compliance.DocumentCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Vertical slice handler: owns ALL business logic for UploadDocument.
 * No service class sitting between controller and repository.
 * Single responsibility: validate → persist → publish.
 */
@Component
public class UploadDocumentHandler {

    private static final Logger log = LoggerFactory.getLogger(UploadDocumentHandler.class);

    private final DocumentRepository repository;
    private final EventPublisher      eventPublisher;

    public UploadDocumentHandler(DocumentRepository repository,
                                 EventPublisher eventPublisher) {
        this.repository    = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UploadDocumentResult handle(UploadDocumentCommand cmd) {
        // ── Business rule: date range must be valid ───────────────────────────
        if (cmd.dataRilascio() != null && cmd.dataScadenza() != null
                && cmd.dataScadenza().isBefore(cmd.dataRilascio())) {
            return new UploadDocumentResult.InvalidDateRange(
                    cmd.dataRilascio(), cmd.dataScadenza(),
                    "dataScadenza must be equal to or after dataRilascio");
        }

        var entity = DocumentEntity.builder()
                .personId(cmd.personId())
                .asdId(cmd.asdId())
                .tipo(cmd.tipo())
                .dataRilascio(cmd.dataRilascio())
                .dataScadenza(cmd.dataScadenza())
                .stato(DocumentStatus.VALID)
                .numero(cmd.numero())
                .enteRilascio(cmd.enteRilascio())
                .fileUrl(cmd.fileUrl())
                .note(cmd.note())
                .build();

        var saved = repository.save(entity);
        log.info("[UPLOAD_DOCUMENT] Saved documentId={} tipo={} personId={}",
                saved.getId(), saved.getTipo(), saved.getPersonId());

        // Publish creation event (Finance and others may listen)
        eventPublisher.publish(
                KafkaTopics.DOCUMENT_CREATED,
                new DocumentCreatedEvent(
                        UUID.randomUUID(), saved.getId(), saved.getPersonId(), saved.getAsdId(),
                        saved.getTipo().name(), saved.getDataScadenza(), Instant.now()),
                saved.getAsdId(), null);

        return new UploadDocumentResult.Success(
                saved.getId(), saved.getPersonId(), saved.getAsdId(),
                saved.getTipo().name(), saved.getDataScadenza());
    }
}
