package it.asd.compliance.features.expirycheck;

import it.asd.common.kafka.EventPublisher;
import it.asd.compliance.shared.entity.DocumentStatus;
import it.asd.compliance.shared.repository.DocumentRepository;
import it.asd.events.KafkaTopics;
import it.asd.events.compliance.DocumentExpiredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.UUID;

@Component
public class ExpiryCheckHandler {

    private static final Logger log = LoggerFactory.getLogger(ExpiryCheckHandler.class);
    private static final int EXPIRY_WARNING_DAYS = 30;

    private final DocumentRepository repository;
    private final EventPublisher eventPublisher;

    public ExpiryCheckHandler(DocumentRepository repository,
                              EventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ExpiryCheckResult handle() {
        var threshold = LocalDate.now().plusDays(EXPIRY_WARNING_DAYS);
        var docs = repository.findExpiringOrExpired(threshold);
        var failed = new ArrayList<UUID>();
        int expired = 0;
        int expiringSoon = 0;

        for (var doc : docs) {
            try {
                if (doc.isExpired()) {
                    doc.setStato(DocumentStatus.EXPIRED);
                    repository.save(doc);
                    eventPublisher.publish(
                            KafkaTopics.DOCUMENT_EXPIRED,
                            new DocumentExpiredEvent(
                                    UUID.randomUUID(), doc.getPersonId(), doc.getAsdId(),
                                    doc.getId(), doc.getTipo().name(),
                                    doc.getDataScadenza(), Instant.now()),
                            doc.getAsdId(), null);
                    expired++;
                } else if (doc.isExpiringSoon(EXPIRY_WARNING_DAYS)) {
                    doc.setStato(DocumentStatus.EXPIRING_SOON);
                    repository.save(doc);
                    expiringSoon++;
                }
            } catch (Exception ex) {
                log.error("[EXPIRY_JOB] Failed processing documentId={}: {}",
                        doc.getId(), ex.getMessage(), ex);
                failed.add(doc.getId());
            }
        }

        return new ExpiryCheckResult.Summary(expired, expiringSoon, failed);
    }
}
