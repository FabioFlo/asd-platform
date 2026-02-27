package it.asd.common.kafka;

import it.asd.events.DomainEvent;
import it.asd.events.EventEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Single publishing entry point. Partition key = aggregateId
 * to preserve ordering per aggregate across topic partitions.
 * <p>
 * No Lombok — Logger via LoggerFactory, fields via constructor injection.
 */
@Component
public final class EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);

    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;
    private final String serviceName;

    public EventPublisher(
            KafkaTemplate<String, EventEnvelope> kafkaTemplate,
            @Value("${spring.application.name}") String serviceName) {
        this.kafkaTemplate = kafkaTemplate;
        this.serviceName = serviceName;
    }

    public CompletableFuture<SendResult<String, EventEnvelope>> publish(
            String topic, DomainEvent event) {
        return publish(topic, event, null, null);
    }

    public CompletableFuture<SendResult<String, EventEnvelope>> publish(
            String topic, DomainEvent event, UUID asdId, UUID seasonId) {
        var envelope = EventEnvelope.of(event, serviceName)
                .withAsdContext(asdId, seasonId);

        log.info("[EVENT] {} → {} (key={})",
                event.getClass().getSimpleName(), topic, event.aggregateId());

        return kafkaTemplate.send(topic, event.aggregateId(), envelope)
                .whenComplete((r, ex) -> {
                    if (ex != null)
                        log.error("[EVENT] Publish failed topic={} error={}", topic, ex.getMessage());
                    else
                        log.debug("[EVENT] Delivered {}@offset={}", topic,
                                r.getRecordMetadata().offset());
                });
    }
}
