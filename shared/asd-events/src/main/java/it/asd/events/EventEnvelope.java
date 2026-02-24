package it.asd.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Transport wrapper around every DomainEvent published to Kafka.
 * Carries routing metadata without polluting the event record itself.
 *
 * Immutable record — created once, never mutated.
 * Use the fluent `with*` methods to add context before publishing.
 */
public record EventEnvelope(
        UUID    envelopeId,
        Instant occurredAt,
        String  source,
        UUID    asdId,       // null for non-ASD-scoped events
        UUID    seasonId,    // null for non-season-scoped events
        String  correlationId,
        DomainEvent payload
) {
    /** Factory: minimal envelope from a domain event + publishing service name */
    public static EventEnvelope of(DomainEvent event, String source) {
        return new EventEnvelope(
                UUID.randomUUID(), Instant.now(), source,
                null, null, null, event);
    }

    public EventEnvelope withAsdContext(UUID asdId, UUID seasonId) {
        return new EventEnvelope(envelopeId, occurredAt, source,
                asdId, seasonId, correlationId, payload);
    }

    public EventEnvelope withCorrelationId(String correlationId) {
        return new EventEnvelope(envelopeId, occurredAt, source,
                asdId, seasonId, correlationId, payload);
    }
}
