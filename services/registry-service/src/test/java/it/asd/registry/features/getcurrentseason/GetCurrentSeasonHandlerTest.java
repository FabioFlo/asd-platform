package it.asd.registry.features.getcurrentseason;

import it.asd.registry.shared.TestFixtures;
import it.asd.registry.shared.entity.SeasonStatus;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetCurrentSeasonHandler")
@Tag("unit")
class GetCurrentSeasonHandlerTest {

    @Mock
    private SeasonRepository seasonRepository;

    @InjectMocks
    private GetCurrentSeasonHandler handler;

    @Nested
    @DisplayName("when an active season exists")
    class WhenFound {

        @Test
        @DisplayName("returns Found with season details")
        void returnsFound() {
            var asdId = UUID.randomUUID();
            var season = TestFixtures.savedSeason(UUID.randomUUID(), asdId);
            when(seasonRepository.findByAsdIdAndStato(asdId, SeasonStatus.ACTIVE))
                    .thenReturn(Optional.of(season));

            var result = handler.handle(new GetCurrentSeasonQuery(asdId));

            assertThat(result).isInstanceOf(GetCurrentSeasonResult.Found.class);
            var found = (GetCurrentSeasonResult.Found) result;
            assertThat(found.codice()).isEqualTo("2025-2026");
        }
    }

    @Nested
    @DisplayName("when no active season exists")
    class WhenNoActiveSeason {

        @Test
        @DisplayName("returns NoActiveSeason")
        void returnsNoActiveSeason() {
            var asdId = UUID.randomUUID();
            when(seasonRepository.findByAsdIdAndStato(asdId, SeasonStatus.ACTIVE))
                    .thenReturn(Optional.empty());

            var result = handler.handle(new GetCurrentSeasonQuery(asdId));

            assertThat(result).isInstanceOf(GetCurrentSeasonResult.NoActiveSeason.class);
        }
    }
}
