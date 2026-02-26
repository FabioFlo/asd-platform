package it.asd.finance.features.confirmpayment;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.finance.shared.TestFixtures;
import it.asd.finance.shared.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ConfirmPaymentHandler")
@Tag("unit")
class ConfirmPaymentHandlerTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ConfirmPaymentHandler handler;

    @Nested
    @DisplayName("when payment is PENDING")
    class WhenPending {

        @Test
        @DisplayName("returns Confirmed, saves updated entity, and publishes event")
        void returnsConfirmed() {
            // arrange
            var paymentId = UUID.randomUUID();
            var entity = TestFixtures.pendingPayment(paymentId);
            var cmd = TestFixtures.confirmPaymentCommandFor(paymentId);

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(entity));
            when(paymentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // act
            var result = handler.handle(cmd);

            // assert
            assertThat(result).isInstanceOf(ConfirmPaymentResult.Confirmed.class);
            var confirmed = (ConfirmPaymentResult.Confirmed) result;
            assertThat(confirmed.paymentId()).isEqualTo(paymentId);
            assertThat(confirmed.importo()).isEqualByComparingTo("50.00");

            verify(paymentRepository).save(any());
            verify(eventPublisher).publish(eq(KafkaTopics.PAYMENT_CONFIRMED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when payment does not exist")
    class WhenNotFound {

        @Test
        @DisplayName("returns NotFound without saving or publishing")
        void returnsNotFound() {
            // arrange
            var paymentId = UUID.randomUUID();
            var cmd = TestFixtures.confirmPaymentCommandFor(paymentId);

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

            // act
            var result = handler.handle(cmd);

            // assert
            assertThat(result).isInstanceOf(ConfirmPaymentResult.NotFound.class);
            var notFound = (ConfirmPaymentResult.NotFound) result;
            assertThat(notFound.paymentId()).isEqualTo(paymentId);

            verify(paymentRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when payment is already CONFIRMED")
    class WhenAlreadyConfirmed {

        @Test
        @DisplayName("returns AlreadyConfirmed without saving or publishing")
        void returnsAlreadyConfirmed() {
            // arrange
            var paymentId = UUID.randomUUID();
            var entity = TestFixtures.confirmedPayment(paymentId);
            var cmd = TestFixtures.confirmPaymentCommandFor(paymentId);

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(entity));

            // act
            var result = handler.handle(cmd);

            // assert
            assertThat(result).isInstanceOf(ConfirmPaymentResult.AlreadyConfirmed.class);

            verify(paymentRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when payment is CANCELLED")
    class WhenAlreadyCancelled {

        @Test
        @DisplayName("returns AlreadyCancelled without saving or publishing")
        void returnsAlreadyCancelled() {
            // arrange
            var paymentId = UUID.randomUUID();
            var entity = TestFixtures.cancelledPayment(paymentId);
            var cmd = TestFixtures.confirmPaymentCommandFor(paymentId);

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(entity));

            // act
            var result = handler.handle(cmd);

            // assert
            assertThat(result).isInstanceOf(ConfirmPaymentResult.AlreadyCancelled.class);

            verify(paymentRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }
}
