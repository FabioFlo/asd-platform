package it.asd.finance.shared;

import it.asd.events.EventEnvelope;
import it.asd.events.competition.ParticipantRegisteredEvent;
import it.asd.events.membership.GroupEnrollmentAddedEvent;
import it.asd.events.membership.MembershipActivatedEvent;
import it.asd.finance.features.confirmpayment.ConfirmPaymentCommand;
import it.asd.finance.shared.entity.PaymentEntity;
import it.asd.finance.shared.entity.PaymentStatus;
import it.asd.finance.shared.entity.PaymentType;
import it.asd.finance.shared.entity.FeeRuleEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Centralized test data for finance-service.
 * All test classes import from here — never build entities inline.
 *
 * Keep builders minimal: use sensible defaults, let callers override
 * only what matters for their specific test case.
 */
public final class TestFixtures {

    private TestFixtures() {}

    // ── Stable IDs ────────────────────────────────────────────────────────────

    public static final UUID PERSON_ID     = UUID.fromString("11111111-1111-1111-1111-111111111111");
    public static final UUID ASD_ID        = UUID.fromString("22222222-2222-2222-2222-222222222222");
    public static final UUID SEASON_ID     = UUID.fromString("33333333-3333-3333-3333-333333333333");
    public static final UUID MEMBERSHIP_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    public static final UUID ENROLLMENT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    public static final UUID PARTICIPATION_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    public static final UUID PAYMENT_ID    = UUID.fromString("77777777-7777-7777-7777-777777777777");
    public static final UUID GROUP_ID      = UUID.fromString("88888888-8888-8888-8888-888888888888");
    public static final UUID COMPETITION_EVENT_ID = UUID.fromString("99999999-9999-9999-9999-999999999999");

    // ── Commands ──────────────────────────────────────────────────────────────

    public static ConfirmPaymentCommand validConfirmPaymentCommand() {
        return new ConfirmPaymentCommand(
                PAYMENT_ID,
                LocalDate.now(),
                "BONIFICO",
                "RIF-001",
                null
        );
    }

    public static ConfirmPaymentCommand confirmPaymentCommandFor(UUID paymentId) {
        return new ConfirmPaymentCommand(
                paymentId,
                LocalDate.now(),
                "BONIFICO",
                "RIF-001",
                null
        );
    }

    // ── Entities (for repository stubs in unit tests) ─────────────────────────

    public static PaymentEntity pendingPayment(UUID id) {
        return PaymentEntity.builder()
                .id(id)
                .personId(PERSON_ID)
                .asdId(ASD_ID)
                .seasonId(SEASON_ID)
                .triggerEventId(MEMBERSHIP_ID)
                .triggerType("membership.activated")
                .paymentType(PaymentType.QUOTA_ASSOCIATIVA)
                .importo(new BigDecimal("50.00"))
                .dataScadenza(LocalDate.now().plusDays(30))
                .stato(PaymentStatus.PENDING)
                .build();
    }

    public static PaymentEntity pendingPayment() {
        return pendingPayment(PAYMENT_ID);
    }

    public static PaymentEntity confirmedPayment(UUID id) {
        return PaymentEntity.builder()
                .id(id)
                .personId(PERSON_ID)
                .asdId(ASD_ID)
                .seasonId(SEASON_ID)
                .triggerEventId(MEMBERSHIP_ID)
                .triggerType("membership.activated")
                .paymentType(PaymentType.QUOTA_ASSOCIATIVA)
                .importo(new BigDecimal("50.00"))
                .dataScadenza(LocalDate.now().plusDays(30))
                .dataPagamento(LocalDate.now())
                .stato(PaymentStatus.CONFIRMED)
                .metodoPagamento("BONIFICO")
                .build();
    }

    public static PaymentEntity confirmedPayment() {
        return confirmedPayment(PAYMENT_ID);
    }

    public static PaymentEntity cancelledPayment(UUID id) {
        return PaymentEntity.builder()
                .id(id)
                .personId(PERSON_ID)
                .asdId(ASD_ID)
                .seasonId(SEASON_ID)
                .triggerEventId(MEMBERSHIP_ID)
                .triggerType("membership.activated")
                .paymentType(PaymentType.QUOTA_ASSOCIATIVA)
                .importo(new BigDecimal("50.00"))
                .dataScadenza(LocalDate.now().plusDays(30))
                .stato(PaymentStatus.CANCELLED)
                .build();
    }

    public static PaymentEntity cancelledPayment() {
        return cancelledPayment(PAYMENT_ID);
    }

    public static PaymentEntity overduePayment(UUID id) {
        return PaymentEntity.builder()
                .id(id)
                .personId(PERSON_ID)
                .asdId(ASD_ID)
                .seasonId(SEASON_ID)
                .triggerEventId(MEMBERSHIP_ID)
                .triggerType("membership.activated")
                .paymentType(PaymentType.QUOTA_ASSOCIATIVA)
                .importo(new BigDecimal("50.00"))
                .dataScadenza(LocalDate.now().minusDays(5))
                .stato(PaymentStatus.PENDING)
                .build();
    }

    public static PaymentEntity overduePayment() {
        return overduePayment(UUID.randomUUID());
    }

    public static FeeRuleEntity activeFeeRule(PaymentType type, BigDecimal importo, int giorniScadenza) {
        return FeeRuleEntity.builder()
                .id(UUID.randomUUID())
                .asdId(ASD_ID)
                .seasonId(SEASON_ID)
                .paymentType(type)
                .importo(importo)
                .giorniScadenza(giorniScadenza)
                .attivo(true)
                .build();
    }

    // ── Kafka event objects ───────────────────────────────────────────────────

    public static MembershipActivatedEvent membershipActivatedEvent() {
        return new MembershipActivatedEvent(
                UUID.randomUUID(),
                MEMBERSHIP_ID,
                PERSON_ID,
                ASD_ID,
                SEASON_ID,
                "TESSERA-001",
                LocalDate.now(),
                Instant.now()
        );
    }

    public static MembershipActivatedEvent membershipActivatedEventWith(UUID membershipId) {
        return new MembershipActivatedEvent(
                UUID.randomUUID(),
                membershipId,
                PERSON_ID,
                ASD_ID,
                SEASON_ID,
                "TESSERA-001",
                LocalDate.now(),
                Instant.now()
        );
    }

    public static GroupEnrollmentAddedEvent groupEnrollmentAddedEvent() {
        return new GroupEnrollmentAddedEvent(
                UUID.randomUUID(),
                ENROLLMENT_ID,
                PERSON_ID,
                GROUP_ID,
                ASD_ID,
                SEASON_ID,
                "ATLETA",
                LocalDate.now(),
                Instant.now()
        );
    }

    public static GroupEnrollmentAddedEvent groupEnrollmentAddedEventWith(UUID enrollmentId) {
        return new GroupEnrollmentAddedEvent(
                UUID.randomUUID(),
                enrollmentId,
                PERSON_ID,
                GROUP_ID,
                ASD_ID,
                SEASON_ID,
                "ATLETA",
                LocalDate.now(),
                Instant.now()
        );
    }

    public static ParticipantRegisteredEvent participantRegisteredEvent() {
        return new ParticipantRegisteredEvent(
                UUID.randomUUID(),
                PARTICIPATION_ID,
                COMPETITION_EVENT_ID,
                PERSON_ID,
                GROUP_ID,
                ASD_ID,
                SEASON_ID,
                "SENIOR",
                Instant.now()
        );
    }

    public static ParticipantRegisteredEvent participantRegisteredEventWith(UUID participationId) {
        return new ParticipantRegisteredEvent(
                UUID.randomUUID(),
                participationId,
                COMPETITION_EVENT_ID,
                PERSON_ID,
                GROUP_ID,
                ASD_ID,
                SEASON_ID,
                "SENIOR",
                Instant.now()
        );
    }

    public static EventEnvelope envelopeOf(it.asd.events.DomainEvent event) {
        return EventEnvelope.of(event, "test")
                .withAsdContext(ASD_ID, SEASON_ID);
    }
}
