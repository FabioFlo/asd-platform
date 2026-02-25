package it.asd.events.compliance;

import it.asd.events.DomainEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DocumentCreatedEvent(
        UUID eventId, UUID documentId, UUID personId, UUID asdId,
        String documentType, LocalDate dataScadenza, Instant occurredAt
) implements DomainEvent {
    @Override public String aggregateId()   { return documentId.toString(); }
    @Override public String aggregateType() { return "Document"; }
}
