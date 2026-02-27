package it.asd.events.registry;

import it.asd.events.DomainEvent;

import java.time.Instant;
import java.util.UUID;

public record SeasonActivatedEvent(
        UUID eventId, UUID asdId, UUID seasonId, String codice, Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateId() {
        return seasonId.toString();
    }

    @Override
    public String aggregateType() {
        return "SeasonEntity";
    }
}
