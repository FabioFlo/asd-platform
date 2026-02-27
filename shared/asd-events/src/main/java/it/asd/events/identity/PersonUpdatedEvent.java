package it.asd.events.identity;

import it.asd.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record PersonUpdatedEvent(
        UUID eventId, UUID personId, String nome, String cognome,
        String email, Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateId() {
        return personId.toString();
    }

    @Override
    public String aggregateType() {
        return "PersonEntity";
    }
}
