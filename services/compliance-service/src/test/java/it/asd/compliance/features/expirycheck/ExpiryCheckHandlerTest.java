package it.asd.compliance.features.expirycheck;

import it.asd.common.kafka.EventPublisher;
import it.asd.compliance.shared.TestFixtures;
import it.asd.compliance.shared.repository.DocumentRepository;
import it.asd.events.KafkaTopics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ExpiryCheckHandler")
@Tag("unit")
class ExpiryCheckHandlerTest {

    @Mock
    private DocumentRepository repository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ExpiryCheckHandler handler;

    @Nested
    @DisplayName("when no documents need processing")
    class WhenEmpty {

        @Test
        @DisplayName("returns Summary with zero counts")
        void returnsSummaryWithZeroCounts() {
            when(repository.findExpiringOrExpired(any())).thenReturn(List.of());

            var result = handler.handle();

            assertThat(result).isInstanceOf(ExpiryCheckResult.Summary.class);
            var summary = (ExpiryCheckResult.Summary) result;
            assertThat(summary.expiredCount()).isZero();
            assertThat(summary.expiringSoonCount()).isZero();
            assertThat(summary.failedDocumentIds()).isEmpty();
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when expired documents exist")
    class WhenExpiredDocuments {

        @Test
        @DisplayName("marks them EXPIRED, saves, and publishes DOCUMENT_EXPIRED event")
        void processesExpiredDocuments() {
            var expiredDoc = TestFixtures.expiredDocument(UUID.randomUUID());
            when(repository.findExpiringOrExpired(any())).thenReturn(List.of(expiredDoc));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var result = handler.handle();

            var summary = (ExpiryCheckResult.Summary) result;
            assertThat(summary.expiredCount()).isEqualTo(1);
            assertThat(summary.expiringSoonCount()).isZero();
            verify(repository).save(expiredDoc);
            verify(eventPublisher).publish(eq(KafkaTopics.DOCUMENT_EXPIRED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when expiring-soon documents exist")
    class WhenExpiringSoonDocuments {

        @Test
        @DisplayName("marks them EXPIRING_SOON, saves, and does not publish DOCUMENT_EXPIRED")
        void processesExpiringSoonDocuments() {
            var expiringSoon = TestFixtures.expiringSoonDocument(UUID.randomUUID());
            when(repository.findExpiringOrExpired(any())).thenReturn(List.of(expiringSoon));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var result = handler.handle();

            var summary = (ExpiryCheckResult.Summary) result;
            assertThat(summary.expiringSoonCount()).isEqualTo(1);
            assertThat(summary.expiredCount()).isZero();
            verify(repository).save(expiringSoon);
            verify(eventPublisher, never()).publish(eq(KafkaTopics.DOCUMENT_EXPIRED), any(), any(), any());
        }
    }
}
