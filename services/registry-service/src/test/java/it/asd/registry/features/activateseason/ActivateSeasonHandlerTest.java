package it.asd.registry.features.activateseason;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.registry.shared.TestFixtures;
import it.asd.registry.shared.entity.SeasonEntity;
import it.asd.registry.shared.entity.SeasonStatus;
import it.asd.registry.shared.repository.AsdRepository;
import it.asd.registry.shared.repository.SeasonRepository;
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
@DisplayName("ActivateSeasonHandler")
@Tag("unit")
class ActivateSeasonHandlerTest {

    @Mock
    private AsdRepository asdRepository;

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ActivateSeasonHandler handler;

    @Nested
    @DisplayName("when ASD exists and no active season")
    class WhenValid {

        @Test
        @DisplayName("returns Activated, saves season, and publishes event")
        void returnsActivated() {
            var asdId = UUID.randomUUID();
            var cmd = TestFixtures.validActivateSeasonCommand(asdId);
            when(asdRepository.findById(asdId)).thenReturn(Optional.of(TestFixtures.savedAsd(asdId)));
            when(seasonRepository.existsByAsdIdAndCodice(asdId, cmd.codice())).thenReturn(false);
            when(seasonRepository.findByAsdIdAndStato(asdId, SeasonStatus.ACTIVE)).thenReturn(Optional.empty());
            when(seasonRepository.save(any())).thenAnswer(inv -> {
                SeasonEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ActivateSeasonResult.Activated.class);
            verify(seasonRepository).save(any());
            verify(eventPublisher).publish(eq(KafkaTopics.SEASON_ACTIVATED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when ASD does not exist")
    class WhenAsdNotFound {

        @Test
        @DisplayName("returns AsdNotFound without saving")
        void returnsAsdNotFound() {
            var asdId = UUID.randomUUID();
            var cmd = TestFixtures.validActivateSeasonCommand(asdId);
            when(asdRepository.findById(asdId)).thenReturn(Optional.empty());

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ActivateSeasonResult.AsdNotFound.class);
            verify(seasonRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when ASD already has an active season")
    class WhenAlreadyHasActiveSeason {

        @Test
        @DisplayName("returns AlreadyHasActiveSeason without saving")
        void returnsAlreadyHasActiveSeason() {
            var asdId = UUID.randomUUID();
            var cmd = TestFixtures.validActivateSeasonCommand(asdId);
            var existingSeason = TestFixtures.savedSeason(UUID.randomUUID(), asdId);
            when(asdRepository.findById(asdId)).thenReturn(Optional.of(TestFixtures.savedAsd(asdId)));
            when(seasonRepository.existsByAsdIdAndCodice(asdId, cmd.codice())).thenReturn(false);
            when(seasonRepository.findByAsdIdAndStato(asdId, SeasonStatus.ACTIVE))
                    .thenReturn(Optional.of(existingSeason));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ActivateSeasonResult.AlreadyHasActiveSeason.class);
            verify(seasonRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when date range is invalid")
    class WhenInvalidDateRange {

        @Test
        @DisplayName("returns InvalidDateRange without saving")
        void returnsInvalidDateRange() {
            var asdId = UUID.randomUUID();
            var cmd = TestFixtures.commandWithInvalidDateRange(asdId);
            when(asdRepository.findById(asdId)).thenReturn(Optional.of(TestFixtures.savedAsd(asdId)));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ActivateSeasonResult.InvalidDateRange.class);
            verify(seasonRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when season codice already exists for this ASD")
    class WhenDuplicateCodice {

        @Test
        @DisplayName("returns DuplicateCodice without saving")
        void returnsDuplicateCodice() {
            var asdId = UUID.randomUUID();
            var cmd = TestFixtures.validActivateSeasonCommand(asdId);
            when(asdRepository.findById(asdId)).thenReturn(Optional.of(TestFixtures.savedAsd(asdId)));
            when(seasonRepository.existsByAsdIdAndCodice(asdId, cmd.codice())).thenReturn(true);

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ActivateSeasonResult.DuplicateCodice.class);
            verify(seasonRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }
}
