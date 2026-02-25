package it.asd.events.scheduling;

import it.asd.events.DomainEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record SessionScheduledEvent(
        UUID eventId, UUID sessionId, UUID asdId, UUID groupId,
        UUID venueId, LocalDate data, LocalTime oraInizio, LocalTime oraFine,
        String tipo, Instant occurredAt
) implements DomainEvent {
    @Override public String aggregateId()   { return sessionId.toString(); }
    @Override public String aggregateType() { return "SessionEntity"; }
}
