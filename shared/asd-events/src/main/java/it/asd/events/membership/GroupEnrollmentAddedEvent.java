package it.asd.events.membership;

import it.asd.events.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record GroupEnrollmentAddedEvent(
        UUID eventId, UUID enrollmentId, UUID personId, UUID groupId,
        UUID asdId, UUID seasonId, String ruolo, LocalDate dataIngresso, Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateId() {
        return enrollmentId.toString();
    }

    @Override
    public String aggregateType() {
        return "GroupEnrollmentEntity";
    }
}
