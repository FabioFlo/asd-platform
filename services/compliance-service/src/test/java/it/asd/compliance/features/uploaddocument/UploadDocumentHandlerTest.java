package it.asd.compliance.features.uploaddocument;

import it.asd.common.kafka.EventPublisher;
import it.asd.compliance.shared.TestFixtures;
import it.asd.compliance.shared.entity.DocumentEntity;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UploadDocumentHandler")
@Tag("unit")
class UploadDocumentHandlerTest {

    @Mock
    private DocumentRepository repository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private UploadDocumentHandler handler;

    @Nested
    @DisplayName("when date range is valid")
    class WhenValid {

        @Test
        @DisplayName("returns Success, saves document, and publishes event")
        void returnsSuccess() {
            var cmd = TestFixtures.validUploadDocumentCommand();
            when(repository.save(any())).thenAnswer(inv -> {
                DocumentEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(UploadDocumentResult.Success.class);
            verify(repository).save(any());
            verify(eventPublisher).publish(eq(KafkaTopics.DOCUMENT_CREATED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when data scadenza is before data rilascio")
    class WhenInvalidDateRange {

        @Test
        @DisplayName("returns InvalidDateRange without saving")
        void returnsInvalidDateRange() {
            var cmd = TestFixtures.commandWithInvalidDateRange();

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(UploadDocumentResult.InvalidDateRange.class);
            verify(repository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }
}
