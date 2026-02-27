package it.asd.events.compliance;

import it.asd.events.DomainEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PersonIneligibleEvent(
        UUID eventId, UUID personId, UUID asdId,
        List<String> blockingDocumentTypes, Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateId() {
        return personId.toString();
    }

    @Override
    public String aggregateType() {
        return "Person";
    }
}
