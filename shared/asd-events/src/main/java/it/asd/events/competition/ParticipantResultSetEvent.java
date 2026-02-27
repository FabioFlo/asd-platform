package it.asd.events.competition;

import it.asd.events.DomainEvent;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ParticipantResultSetEvent(
        UUID eventId, UUID participationId, UUID competitionEventId,
        UUID personId, UUID groupId, String disciplina,
        Integer posizione, Double punteggio,
        Map<String, Object> resultData, Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateId() {
        return participationId.toString();
    }

    @Override
    public String aggregateType() {
        return "EventParticipation";
    }
}
