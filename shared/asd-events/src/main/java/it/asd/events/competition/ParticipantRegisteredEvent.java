package it.asd.events.competition;

import it.asd.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record ParticipantRegisteredEvent(
        UUID eventId, UUID participationId, UUID competitionEventId,
        UUID personId, UUID groupId, UUID asdId, UUID seasonId,
        String categoria, Instant occurredAt
) implements DomainEvent {
    @Override public String aggregateId()   { return participationId.toString(); }
    @Override public String aggregateType() { return "EventParticipation"; }
}
