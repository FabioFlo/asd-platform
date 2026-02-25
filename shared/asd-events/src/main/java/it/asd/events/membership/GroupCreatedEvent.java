package it.asd.events.membership;

import it.asd.events.DomainEvent;
import java.time.Instant;
import java.util.UUID;

public record GroupCreatedEvent(
        UUID eventId, UUID groupId, UUID asdId, UUID seasonId,
        String nome, String disciplina, String tipo, Instant occurredAt
) implements DomainEvent {
    @Override public String aggregateId()   { return groupId.toString(); }
    @Override public String aggregateType() { return "GroupEntity"; }
}
