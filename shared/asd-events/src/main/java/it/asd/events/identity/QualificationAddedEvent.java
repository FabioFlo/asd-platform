package it.asd.events.identity;

import it.asd.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record QualificationAddedEvent(
        UUID eventId, UUID qualificationId, UUID personId,
        String tipo, String ente, String livello, Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateId() {
        return qualificationId.toString();
    }

    @Override
    public String aggregateType() {
        return "QualificationEntity";
    }
}
