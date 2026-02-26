package it.asd.compliance.features.renewdocument;

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

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RenewDocumentHandler")
@Tag("unit")
class RenewDocumentHandlerTest {

    @Mock
    private DocumentRepository repository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private RenewDocumentHandler handler;

    @Nested
    @DisplayName("when document exists and dates are valid")
    class WhenValid {

        @Test
        @DisplayName("returns Renewed, saves document, and publishes event")
        void returnsRenewed() {
            var documentId = UUID.randomUUID();
            var doc = TestFixtures.validDocument(documentId);
            var cmd = new RenewDocumentCommand(
                    documentId,
                    LocalDate.now(),
                    LocalDate.now().plusYears(1),
                    "NEW-NUM-001", null);
            when(repository.findById(documentId)).thenReturn(Optional.of(doc));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RenewDocumentResult.Renewed.class);
            verify(repository).save(any());
            verify(eventPublisher).publish(eq(KafkaTopics.DOCUMENT_RENEWED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when document does not exist")
    class WhenNotFound {

        @Test
        @DisplayName("returns NotFound without saving")
        void returnsNotFound() {
            var documentId = UUID.randomUUID();
            var cmd = new RenewDocumentCommand(
                    documentId, LocalDate.now(), LocalDate.now().plusYears(1), null, null);
            when(repository.findById(documentId)).thenReturn(Optional.empty());

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RenewDocumentResult.NotFound.class);
            verify(repository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when new data scadenza is before new data rilascio")
    class WhenInvalidDateRange {

        @Test
        @DisplayName("returns InvalidDateRange without saving")
        void returnsInvalidDateRange() {
            var documentId = UUID.randomUUID();
            var doc = TestFixtures.validDocument(documentId);
            var cmd = new RenewDocumentCommand(
                    documentId,
                    LocalDate.now().plusDays(10),   // rilascio after scadenza
                    LocalDate.now(),                 // scadenza before rilascio
                    null, null);
            when(repository.findById(documentId)).thenReturn(Optional.of(doc));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RenewDocumentResult.InvalidDateRange.class);
            verify(repository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }
}
