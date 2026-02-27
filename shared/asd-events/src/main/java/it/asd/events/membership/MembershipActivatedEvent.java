package it.asd.events.membership;

import it.asd.events.DomainEvent;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record MembershipActivatedEvent(
        UUID eventId, UUID membershipId, UUID personId, UUID asdId, UUID seasonId,
        String numeroTessera, LocalDate dataIscrizione, Instant occurredAt
) implements DomainEvent {
    @Override
    public String aggregateId() {
        return membershipId.toString();
    }

    @Override
    public String aggregateType() {
        return "MembershipEntity";
    }
}
