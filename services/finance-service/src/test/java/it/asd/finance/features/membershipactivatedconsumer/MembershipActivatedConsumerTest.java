package it.asd.finance.features.membershipactivatedconsumer;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.finance.shared.TestFixtures;
import it.asd.finance.shared.entity.FeeRuleEntity;
import it.asd.finance.shared.entity.PaymentEntity;
import it.asd.finance.shared.entity.PaymentStatus;
import it.asd.finance.shared.entity.PaymentType;
import it.asd.finance.shared.repository.FeeRuleRepository;
import it.asd.finance.shared.repository.PaymentRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MembershipActivatedConsumer")
@Tag("unit")
class MembershipActivatedConsumerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FeeRuleRepository feeRuleRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private Acknowledgment ack;

    // Inject @Value field via constructor — the consumer requires the default amount
    private static final BigDecimal DEFAULT_QUOTA = new BigDecimal("50.00");

    // We construct the consumer manually so we can inject the @Value field
    private final MembershipActivatedConsumer consumer =
            new MembershipActivatedConsumer(
                    null, null, null, DEFAULT_QUOTA);  // placeholders — replaced below

    // Re-instantiate with real mocks
    private MembershipActivatedConsumer buildConsumer() {
        return new MembershipActivatedConsumer(
                paymentRepository, feeRuleRepository, eventPublisher, DEFAULT_QUOTA);
    }

    @Nested
    @DisplayName("happy path — new membershipId")
    class WhenNewMembership {

        @Test
        @DisplayName("creates a PENDING payment using the configured fee rule and publishes PAYMENT_CREATED")
        void createsPendingPaymentWithFeeRule() {
            // arrange
            var evt = TestFixtures.membershipActivatedEvent();
            var envelope = TestFixtures.envelopeOf(evt);
            var record = new ConsumerRecord<String, EventEnvelope>(
                    KafkaTopics.MEMBERSHIP_ACTIVATED, 0, 0L, null, envelope);

            var feeRule = TestFixtures.activeFeeRule(PaymentType.QUOTA_ASSOCIATIVA, new BigDecimal("75.00"), 45);

            when(paymentRepository.existsByTriggerEventId(evt.membershipId())).thenReturn(false);
            when(feeRuleRepository.findByAsdIdAndSeasonIdAndPaymentTypeAndAttivo(
                    evt.asdId(), evt.seasonId(), PaymentType.QUOTA_ASSOCIATIVA, true))
                    .thenReturn(Optional.of(feeRule));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // act
            buildConsumer().consume(record, ack);

            // assert — payment created with the fee rule amount
            var captor = ArgumentCaptor.forClass(PaymentEntity.class);
            verify(paymentRepository).save(captor.capture());
            var saved = captor.getValue();
            assertThat(saved.getPersonId()).isEqualTo(evt.personId());
            assertThat(saved.getAsdId()).isEqualTo(evt.asdId());
            assertThat(saved.getSeasonId()).isEqualTo(evt.seasonId());
            assertThat(saved.getTriggerEventId()).isEqualTo(evt.membershipId());
            assertThat(saved.getTriggerType()).isEqualTo("membership.activated");
            assertThat(saved.getPaymentType()).isEqualTo(PaymentType.QUOTA_ASSOCIATIVA);
            assertThat(saved.getImporto()).isEqualByComparingTo("75.00");
            assertThat(saved.getDataScadenza()).isEqualTo(evt.dataIscrizione().plusDays(45));
            assertThat(saved.getStato()).isEqualTo(PaymentStatus.PENDING);

            verify(eventPublisher).publish(eq(KafkaTopics.PAYMENT_CREATED), any(), any(), any());
            verify(ack).acknowledge();
        }

        @Test
        @DisplayName("uses default quota when no active fee rule exists")
        void createsPendingPaymentWithDefaultAmount() {
            // arrange
            var evt = TestFixtures.membershipActivatedEvent();
            var envelope = TestFixtures.envelopeOf(evt);
            var record = new ConsumerRecord<String, EventEnvelope>(
                    KafkaTopics.MEMBERSHIP_ACTIVATED, 0, 0L, null, envelope);

            when(paymentRepository.existsByTriggerEventId(evt.membershipId())).thenReturn(false);
            when(feeRuleRepository.findByAsdIdAndSeasonIdAndPaymentTypeAndAttivo(any(), any(), any(), anyBoolean()))
                    .thenReturn(Optional.empty());
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // act
            buildConsumer().consume(record, ack);

            // assert — uses the default of 50.00 and 30 days
            var captor = ArgumentCaptor.forClass(PaymentEntity.class);
            verify(paymentRepository).save(captor.capture());
            assertThat(captor.getValue().getImporto()).isEqualByComparingTo("50.00");
            assertThat(captor.getValue().getDataScadenza())
                    .isEqualTo(evt.dataIscrizione().plusDays(30));

            verify(eventPublisher).publish(eq(KafkaTopics.PAYMENT_CREATED), any(), any(), any());
            verify(ack).acknowledge();
        }
    }

    @Nested
    @DisplayName("idempotency — membershipId already processed")
    class WhenAlreadyProcessed {

        @Test
        @DisplayName("skips creation and publishing, still acknowledges the message")
        void skipsWhenAlreadyProcessed() {
            // arrange
            var evt = TestFixtures.membershipActivatedEvent();
            var envelope = TestFixtures.envelopeOf(evt);
            var record = new ConsumerRecord<String, EventEnvelope>(
                    KafkaTopics.MEMBERSHIP_ACTIVATED, 0, 0L, null, envelope);

            when(paymentRepository.existsByTriggerEventId(evt.membershipId())).thenReturn(true);

            // act
            buildConsumer().consume(record, ack);

            // assert — no save, no publish
            verify(paymentRepository, never()).save(any());
            verifyNoInteractions(feeRuleRepository);
            verifyNoInteractions(eventPublisher);
            verify(ack).acknowledge();
        }
    }

    @Nested
    @DisplayName("unexpected payload type")
    class WhenWrongPayloadType {

        @Test
        @DisplayName("skips processing and acknowledges when payload is not MembershipActivatedEvent")
        void skipsWrongPayloadType() {
            // arrange — wrap a different event type
            var wrongEvent = TestFixtures.groupEnrollmentAddedEvent();
            var envelope = TestFixtures.envelopeOf(wrongEvent);
            var record = new ConsumerRecord<String, EventEnvelope>(
                    KafkaTopics.MEMBERSHIP_ACTIVATED, 0, 0L, null, envelope);

            // act
            buildConsumer().consume(record, ack);

            // assert
            verifyNoInteractions(paymentRepository);
            verifyNoInteractions(feeRuleRepository);
            verifyNoInteractions(eventPublisher);
            verify(ack).acknowledge();
        }
    }
}
