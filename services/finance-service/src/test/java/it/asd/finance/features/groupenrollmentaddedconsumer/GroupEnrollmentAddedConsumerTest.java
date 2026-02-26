package it.asd.finance.features.groupenrollmentaddedconsumer;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.finance.shared.TestFixtures;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupEnrollmentAddedConsumer")
@Tag("unit")
class GroupEnrollmentAddedConsumerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FeeRuleRepository feeRuleRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private Acknowledgment ack;

    private static final BigDecimal DEFAULT_QUOTA_CORSO = new BigDecimal("30.00");

    private GroupEnrollmentAddedConsumer buildConsumer() {
        return new GroupEnrollmentAddedConsumer(
                paymentRepository, feeRuleRepository, eventPublisher, DEFAULT_QUOTA_CORSO);
    }

    @Nested
    @DisplayName("happy path — new enrollmentId")
    class WhenNewEnrollment {

        @Test
        @DisplayName("creates a PENDING QUOTA_CORSO payment using the fee rule and publishes PAYMENT_CREATED")
        void createsPendingPaymentWithFeeRule() {
            // arrange
            var evt = TestFixtures.groupEnrollmentAddedEvent();
            var envelope = TestFixtures.envelopeOf(evt);
            var record = new ConsumerRecord<String, EventEnvelope>(
                    KafkaTopics.GROUP_ENROLLMENT_ADDED, 0, 0L, null, envelope);

            var feeRule = TestFixtures.activeFeeRule(PaymentType.QUOTA_CORSO, new BigDecimal("60.00"), 20);

            when(paymentRepository.existsByTriggerEventId(evt.enrollmentId())).thenReturn(false);
            when(feeRuleRepository.findByAsdIdAndSeasonIdAndPaymentTypeAndAttivo(
                    evt.asdId(), evt.seasonId(), PaymentType.QUOTA_CORSO, true))
                    .thenReturn(Optional.of(feeRule));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // act
            buildConsumer().consume(record, ack);

            // assert
            var captor = ArgumentCaptor.forClass(PaymentEntity.class);
            verify(paymentRepository).save(captor.capture());
            var saved = captor.getValue();
            assertThat(saved.getPersonId()).isEqualTo(evt.personId());
            assertThat(saved.getAsdId()).isEqualTo(evt.asdId());
            assertThat(saved.getSeasonId()).isEqualTo(evt.seasonId());
            assertThat(saved.getTriggerEventId()).isEqualTo(evt.enrollmentId());
            assertThat(saved.getTriggerType()).isEqualTo("group.enrollment.added");
            assertThat(saved.getPaymentType()).isEqualTo(PaymentType.QUOTA_CORSO);
            assertThat(saved.getImporto()).isEqualByComparingTo("60.00");
            assertThat(saved.getDataScadenza()).isEqualTo(evt.dataIngresso().plusDays(20));
            assertThat(saved.getStato()).isEqualTo(PaymentStatus.PENDING);

            verify(eventPublisher).publish(eq(KafkaTopics.PAYMENT_CREATED), any(), any(), any());
            verify(ack).acknowledge();
        }

        @Test
        @DisplayName("uses default quota-corso when no active fee rule exists")
        void createsPendingPaymentWithDefaultAmount() {
            // arrange
            var evt = TestFixtures.groupEnrollmentAddedEvent();
            var envelope = TestFixtures.envelopeOf(evt);
            var record = new ConsumerRecord<String, EventEnvelope>(
                    KafkaTopics.GROUP_ENROLLMENT_ADDED, 0, 0L, null, envelope);

            when(paymentRepository.existsByTriggerEventId(evt.enrollmentId())).thenReturn(false);
            when(feeRuleRepository.findByAsdIdAndSeasonIdAndPaymentTypeAndAttivo(any(), any(), any(), anyBoolean()))
                    .thenReturn(Optional.empty());
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // act
            buildConsumer().consume(record, ack);

            // assert
            var captor = ArgumentCaptor.forClass(PaymentEntity.class);
            verify(paymentRepository).save(captor.capture());
            assertThat(captor.getValue().getImporto()).isEqualByComparingTo("30.00");
            assertThat(captor.getValue().getDataScadenza())
                    .isEqualTo(evt.dataIngresso().plusDays(30));

            verify(eventPublisher).publish(eq(KafkaTopics.PAYMENT_CREATED), any(), any(), any());
            verify(ack).acknowledge();
        }
    }

    @Nested
    @DisplayName("idempotency — enrollmentId already processed")
    class WhenAlreadyProcessed {

        @Test
        @DisplayName("skips creation and publishing, still acknowledges the message")
        void skipsWhenAlreadyProcessed() {
            // arrange
            var evt = TestFixtures.groupEnrollmentAddedEvent();
            var envelope = TestFixtures.envelopeOf(evt);
            var record = new ConsumerRecord<String, EventEnvelope>(
                    KafkaTopics.GROUP_ENROLLMENT_ADDED, 0, 0L, null, envelope);

            when(paymentRepository.existsByTriggerEventId(evt.enrollmentId())).thenReturn(true);

            // act
            buildConsumer().consume(record, ack);

            // assert
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
        @DisplayName("skips processing and acknowledges when payload is not GroupEnrollmentAddedEvent")
        void skipsWrongPayloadType() {
            // arrange — wrap a MembershipActivatedEvent instead
            var wrongEvent = TestFixtures.membershipActivatedEvent();
            var envelope = TestFixtures.envelopeOf(wrongEvent);
            var record = new ConsumerRecord<String, EventEnvelope>(
                    KafkaTopics.GROUP_ENROLLMENT_ADDED, 0, 0L, null, envelope);

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
