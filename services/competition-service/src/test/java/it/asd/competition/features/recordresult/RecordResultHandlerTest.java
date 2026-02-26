package it.asd.competition.features.recordresult;

import it.asd.common.kafka.EventPublisher;
import it.asd.competition.shared.TestFixtures;
import it.asd.competition.shared.repository.EventParticipationRepository;
import it.asd.events.KafkaTopics;
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
@DisplayName("RecordResultHandler")
@Tag("unit")
class RecordResultHandlerTest {

    @Mock
    private EventParticipationRepository repo;

    @Mock
    private ParticipationMapper mapper;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private RecordResultHandler handler;

    // ── Recorded ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("when participation exists")
    class WhenFound {

        @Test
        @DisplayName("returns Recorded, saves entity, and publishes event")
        void returnsRecorded() {
            var cmd = TestFixtures.validRecordResultCommand();
            var entity = TestFixtures.participationWithResult(cmd.participationId());

            when(repo.findById(cmd.participationId())).thenReturn(Optional.of(entity));
            when(repo.save(any())).thenReturn(entity);
            // mapper.updateFromCommand is void — no stubbing needed (Mockito no-ops void by default)

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RecordResultResult.Recorded.class);
            var recorded = (RecordResultResult.Recorded) result;
            assertThat(recorded.participationId()).isEqualTo(entity.getId());
            verify(mapper).updateFromCommand(eq(cmd), eq(entity));
            verify(repo).save(entity);
            verify(eventPublisher).publish(
                    eq(KafkaTopics.PARTICIPANT_RESULT_SET), any(), any(), any());
        }

        @Test
        @DisplayName("returns Recorded even when punteggio and posizione are null")
        void returnsRecordedWithNullScore() {
            var cmd = TestFixtures.recordResultCommandWithoutScore();
            var entity = TestFixtures.savedParticipation(cmd.participationId());

            when(repo.findById(cmd.participationId())).thenReturn(Optional.of(entity));
            when(repo.save(any())).thenReturn(entity);

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RecordResultResult.Recorded.class);
            verify(eventPublisher).publish(
                    eq(KafkaTopics.PARTICIPANT_RESULT_SET), any(), any(), any());
        }
    }

    // ── NotFound ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("when participation does not exist")
    class WhenNotFound {

        @Test
        @DisplayName("returns NotFound without saving or publishing")
        void returnsNotFound() {
            var unknownId = UUID.randomUUID();
            var cmd = new RecordResultCommand(unknownId, 1, null, null);

            when(repo.findById(unknownId)).thenReturn(Optional.empty());

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RecordResultResult.NotFound.class);
            var notFound = (RecordResultResult.NotFound) result;
            assertThat(notFound.participationId()).isEqualTo(unknownId);
            verify(repo, never()).save(any());
            verifyNoInteractions(mapper);
            verifyNoInteractions(eventPublisher);
        }
    }
}
