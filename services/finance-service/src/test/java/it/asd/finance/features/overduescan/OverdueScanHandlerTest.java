package it.asd.finance.features.overduescan;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.finance.shared.TestFixtures;
import it.asd.finance.shared.entity.PaymentEntity;
import it.asd.finance.shared.entity.PaymentStatus;
import it.asd.finance.shared.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OverdueScanHandler")
@Tag("unit")
class OverdueScanHandlerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private OverdueScanHandler handler;

    @Nested
    @DisplayName("when there are overdue pending payments")
    class WhenOverduePaymentsExist {

        @Test
        @DisplayName("marks all pending payments as OVERDUE and publishes one event per payment")
        void marksAllOverdueAndPublishesEvents() {
            // arrange
            var payment1 = TestFixtures.overduePayment();
            var payment2 = TestFixtures.overduePayment();

            when(paymentRepository.findByStatoAndDataScadenzaBefore(eq(PaymentStatus.PENDING), any()))
                    .thenReturn(List.of(payment1, payment2));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // act
            var result = handler.scan();

            // assert
            assertThat(result).isInstanceOf(OverdueScanResult.Summary.class);
            var summary = (OverdueScanResult.Summary) result;
            assertThat(summary.markedOverdue()).isEqualTo(2);
            assertThat(summary.failed()).isEmpty();

            verify(paymentRepository, times(2)).save(any(PaymentEntity.class));
            verify(eventPublisher, times(2))
                    .publish(eq(KafkaTopics.PAYMENT_OVERDUE), any(), any(), any());
        }

        @Test
        @DisplayName("sets stato to OVERDUE on each saved payment")
        void setsStatusToOverdue() {
            // arrange
            var payment = TestFixtures.overduePayment();

            when(paymentRepository.findByStatoAndDataScadenzaBefore(eq(PaymentStatus.PENDING), any()))
                    .thenReturn(List.of(payment));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // act
            handler.scan();

            // assert — the entity must be mutated before save
            assertThat(payment.getStato()).isEqualTo(PaymentStatus.OVERDUE);
        }
    }

    @Nested
    @DisplayName("when no pending payments are overdue")
    class WhenNoOverduePayments {

        @Test
        @DisplayName("returns Summary with zero markedOverdue and does not publish any event")
        void returnsEmptySummary() {
            // arrange
            when(paymentRepository.findByStatoAndDataScadenzaBefore(eq(PaymentStatus.PENDING), any()))
                    .thenReturn(List.of());

            // act
            var result = handler.scan();

            // assert
            assertThat(result).isInstanceOf(OverdueScanResult.Summary.class);
            var summary = (OverdueScanResult.Summary) result;
            assertThat(summary.markedOverdue()).isZero();
            assertThat(summary.failed()).isEmpty();

            verify(paymentRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when a payment fails to save")
    class WhenSaveFails {

        @Test
        @DisplayName("records failed payment id and continues processing remaining payments")
        void recordsFailedPaymentAndContinues() {
            // arrange
            var failingPayment = TestFixtures.overduePayment();
            var successPayment = TestFixtures.overduePayment();

            when(paymentRepository.findByStatoAndDataScadenzaBefore(eq(PaymentStatus.PENDING), any()))
                    .thenReturn(List.of(failingPayment, successPayment));

            // first save throws, second succeeds
            when(paymentRepository.save(any()))
                    .thenThrow(new RuntimeException("DB error"))
                    .thenAnswer(inv -> inv.getArgument(0));

            // act
            var result = handler.scan();

            // assert
            assertThat(result).isInstanceOf(OverdueScanResult.Summary.class);
            var summary = (OverdueScanResult.Summary) result;
            assertThat(summary.markedOverdue()).isEqualTo(1);
            assertThat(summary.failed()).hasSize(1);
            assertThat(summary.failed()).contains(failingPayment.getId());

            // only the successful payment publishes an event
            verify(eventPublisher, times(1))
                    .publish(eq(KafkaTopics.PAYMENT_OVERDUE), any(), any(), any());
        }
    }
}
