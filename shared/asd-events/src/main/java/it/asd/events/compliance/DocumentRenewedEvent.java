package it.asd.events.compliance;

import it.asd.events.DomainEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record DocumentRenewedEvent(
        UUID eventId, UUID personId, UUID asdId, UUID documentId,
        String documentType, LocalDate newExpiryDate, Instant occurredAt
) implements DomainEvent {
    @Override public String aggregateId()   { return personId.toString(); }
    @Override public String aggregateType() { return "Document"; }
}
