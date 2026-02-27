package it.asd.compliance.features.renewdocument;

import it.asd.common.kafka.EventPublisher;
import it.asd.compliance.shared.entity.DocumentStatus;
import it.asd.compliance.shared.repository.DocumentRepository;
import it.asd.events.KafkaTopics;
import it.asd.events.compliance.DocumentRenewedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class RenewDocumentHandler {

    private final DocumentRepository repository;
    private final EventPublisher eventPublisher;

    public RenewDocumentHandler(DocumentRepository repository,
                                EventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RenewDocumentResult handle(RenewDocumentCommand cmd) {
        var docOpt = repository.findById(cmd.documentId());
        if (docOpt.isEmpty())
            return new RenewDocumentResult.NotFound(cmd.documentId());

        if (cmd.newDataScadenza().isBefore(cmd.newDataRilascio()))
            return new RenewDocumentResult.InvalidDateRange(
                    "newDataScadenza must be equal to or after newDataRilascio");

        var doc = docOpt.get();
        doc.setDataRilascio(cmd.newDataRilascio());
        doc.setDataScadenza(cmd.newDataScadenza());
        doc.setStato(DocumentStatus.VALID);
        if (cmd.newNumero() != null) doc.setNumero(cmd.newNumero());
        if (cmd.newFileUrl() != null) doc.setFileUrl(cmd.newFileUrl());
        repository.save(doc);

        // DocumentRenewedEvent → competition-service updates eligibility cache
        eventPublisher.publish(
                KafkaTopics.DOCUMENT_RENEWED,
                new DocumentRenewedEvent(
                        UUID.randomUUID(), doc.getPersonId(), doc.getAsdId(),
                        doc.getId(), doc.getTipo().name(),
                        doc.getDataScadenza(), Instant.now()),
                doc.getAsdId(), null);

        return new RenewDocumentResult.Renewed(
                doc.getId(), doc.getPersonId(), doc.getAsdId(),
                doc.getTipo().name(), doc.getDataScadenza());
    }
}
