package it.asd.events.registry;

import it.asd.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record AsdCreatedEvent(
        UUID eventId, UUID asdId, String codiceFiscale, String nome,
        String disciplina, Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateId() {
        return asdId.toString();
    }

    @Override
    public String aggregateType() {
        return "AsdEntity";
    }
}
