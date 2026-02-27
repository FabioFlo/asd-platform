package it.asd.competition.features.eligibilitycache;

import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.events.compliance.DocumentExpiredEvent;
import it.asd.events.compliance.DocumentRenewedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

/**
 * Keeps the eligibility cache warm from async compliance events.
 * <p>
 * When a document expires → mark person ineligible in cache.
 * When a document renews  → remove that blocker from cache.
 * <p>
 * This means RegisterParticipantHandler rarely hits Compliance sync —
 * only on the very first registration attempt (cold cache).
 * <p>
 * Manual ack: if processing fails, we do NOT ack → Kafka redelivers.
 * After N failures the message goes to the dead-letter topic (configure
 * a DeadLetterPublishingRecoverer in production).
 */
@Component
public class ComplianceEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ComplianceEventConsumer.class);

    private final EligibilityCacheService cacheService;

    public ComplianceEventConsumer(EligibilityCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @KafkaListener(
            topics = {KafkaTopics.DOCUMENT_EXPIRED, KafkaTopics.DOCUMENT_RENEWED},
            groupId = "competition-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onComplianceEvent(EventEnvelope envelope, Acknowledgment ack) {
        try {
            switch (envelope.payload()) {
                case DocumentExpiredEvent evt -> {
                    cacheService.markIneligible(
                            evt.personId(), evt.asdId(),
                            evt.documentType(), "EXPIRED on " + evt.expiredOn());
                    log.info("[CACHE] Marked ineligible: personId={} doc={}",
                            evt.personId(), evt.documentType());
                }
                case DocumentRenewedEvent evt -> {
                    cacheService.removeBlocker(evt.personId(), evt.asdId(), evt.documentType());
                    log.info("[CACHE] Removed blocker: personId={} doc={}",
                            evt.personId(), evt.documentType());
                }
                // Unknown event type on this topic — log and ack to avoid poison pill
                default -> log.warn("[CACHE] Unhandled event type: {}",
                        envelope.payload().getClass().getSimpleName());
            }
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("[CACHE] Failed processing envelopeId={}: {}",
                    envelope.envelopeId(), ex.getMessage(), ex);
            // Do NOT ack → triggers redelivery
        }
    }
}
