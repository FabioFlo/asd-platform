package it.asd.events.finance;

import it.asd.events.DomainEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentCreatedEvent(
        UUID eventId, UUID paymentId, UUID personId, UUID asdId,
        UUID seasonId, String paymentType, BigDecimal importo,
        LocalDate dataScadenza, Instant occurredAt
) implements DomainEvent {
    @Override public String aggregateId()   { return paymentId.toString(); }
    @Override public String aggregateType() { return "PaymentEntity"; }
}
