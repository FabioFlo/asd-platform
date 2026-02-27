package it.asd.events;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker interface for all domain events.
 *
 * @JsonTypeInfo embeds the concrete class name so consumers
 * can deserialize polymorphically without a type registry.
 * <p>
 * All implementations MUST be Java records — immutable by definition.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@type")
public interface DomainEvent {
    UUID eventId();

    String aggregateId();

    String aggregateType();

    Instant occurredAt();
}
