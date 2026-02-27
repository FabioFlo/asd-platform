package it.asd.finance.features.participantregisteredconsumer;

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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ParticipantRegisteredConsumer")
@Tag("unit")
class ParticipantRegisteredConsumerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private FeeRuleRepository feeRuleRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private Acknowledgment ack;

    private static final BigDecimal DEFAULT_ISCRIZIONE = new BigDecimal("15.00");

    private ParticipantRegisteredConsumer buildConsumer() {
        return new ParticipantRegisteredConsumer(
                paymentRepository, feeRuleRepository, eventPublisher, DEFAULT_ISCRIZIONE);
    }

    @Nested
    @DisplayName("happy path — new participationId")
    class WhenNewParticipation {

        @Test
        @DisplayName("creates a PENDING payment using the configured fee rule and publishes PAYMENT_CREATED")
        void createsPendingPaymentWithFeeRule() {
            var evt = TestFixtures.participantRegisteredEvent();
            var envelope = TestFixtures.envelopeOf(evt);
            var record = new ConsumerRecord<String, EventEnvelope>(
                    KafkaTopics.PARTICIPANT_REGISTERED, 0, 0L, null, envelope);

            var feeRule = TestFixtures.activeFeeRule(PaymentType.ISCRIZIONE_GARA, new BigDecimal("20.00"), 14);

            when(paymentRepository.existsByTriggerEventId(evt.participationId())).thenReturn(false);
            when(feeRuleRepository.findByAsdIdAndSeasonIdAndPaymentTypeAndAttivo(
                    evt.asdId(), evt.seasonId(), PaymentType.ISCRIZIONE_GARA, true))
                    .thenReturn(Optional.of(feeRule));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            buildConsumer().consume(record, ack);

            var captor = ArgumentCaptor.forClass(PaymentEntity.class);
            verify(paymentRepository).save(captor.capture());
            var saved = captor.getValue();
            assertThat(saved.getPersonId()).isEqualTo(evt.personId());
            assertThat(saved.getAsdId()).isEqualTo(evt.asdId());
            assertThat(saved.getSeasonId()).isEqualTo(evt.seasonId());
            assertThat(saved.getTriggerEventId()).isEqualTo(evt.participationId());
            assertThat(saved.getTriggerType()).isEqualTo("competition.participant.registered");
            assertThat(saved.getPaymentType()).isEqualTo(PaymentType.ISCRIZIONE_GARA);
            assertThat(saved.getImporto()).isEqualByComparingTo("20.00");
            assertThat(saved.getStato()).isEqualTo(PaymentStatus.PENDING);

            verify(eventPublisher).publish(eq(KafkaTopics.PAYMENT_CREATED), any(), any(), any());
            verify(ack).acknowledge();
        }

        @Test
        @DisplayName("uses default iscrizione-gara amount when no active fee rule exists")
        void createsPendingPaymentWithDefaultAmount() {
            var evt = TestFixtures.participantRegisteredEvent();
            var envelope = TestFixtures.envelopeOf(evt);
            var record = new ConsumerRecord<String, EventEnvelope>(
                    KafkaTopics.PARTICIPANT_REGISTERED, 0, 0L, null, envelope);

            when(paymentRepository.existsByTriggerEventId(evt.participationId())).thenReturn(false);
            when(feeRuleRepository.findByAsdIdAndSeasonIdAndPaymentTypeAndAttivo(any(), any(), any(), anyBoolean()))
                    .thenReturn(Optional.empty());
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            buildConsumer().consume(record, ack);

            var captor = ArgumentCaptor.forClass(PaymentEntity.class);
            verify(paymentRepository).save(captor.capture());
            assertThat(captor.getValue().getImporto()).isEqualByComparingTo("15.00");

            verify(eventPublisher).publish(eq(KafkaTopics.PAYMENT_CREATED), any(), any(), any());
            verify(ack).acknowledge();
        }
    }

    @Nested
    @DisplayName("idempotency — participationId already processed")
    class WhenAlreadyProcessed {

        @Test
        @DisplayName("skips creation and publishing, still acknowledges the message")
        void skipsWhenAlreadyProcessed() {
            var evt = TestFixtures.participantRegisteredEvent();
            var envelope = TestFixtures.envelopeOf(evt);
            var record = new ConsumerRecord<String, EventEnvelope>(
                    KafkaTopics.PARTICIPANT_REGISTERED, 0, 0L, null, envelope);

            when(paymentRepository.existsByTriggerEventId(evt.participationId())).thenReturn(true);

            buildConsumer().consume(record, ack);

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
        @DisplayName("skips processing and acknowledges when payload is not ParticipantRegisteredEvent")
        void skipsWrongPayloadType() {
            var wrongEvent = TestFixtures.membershipActivatedEvent();
            var envelope = TestFixtures.envelopeOf(wrongEvent);
            var record = new ConsumerRecord<String, EventEnvelope>(
                    KafkaTopics.PARTICIPANT_REGISTERED, 0, 0L, null, envelope);

            buildConsumer().consume(record, ack);

            verifyNoInteractions(paymentRepository);
            verifyNoInteractions(feeRuleRepository);
            verifyNoInteractions(eventPublisher);
            verify(ack).acknowledge();
        }
    }
}
