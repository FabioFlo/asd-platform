package it.asd.scheduling.features.createvenue;

import it.asd.scheduling.shared.TestFixtures;
import it.asd.scheduling.shared.entity.VenueEntity;
import it.asd.scheduling.shared.repository.VenueRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateVenueHandler")
@Tag("unit")
class CreateVenueHandlerTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private CreateVenueHandler handler;

    @Nested
    @DisplayName("when nome is unique for the ASD")
    class WhenValid {

        @Test
        @DisplayName("returns Created and saves the venue entity")
        void returnsCreated() {
            var cmd = TestFixtures.validCreateVenueCommand();
            when(venueRepository.existsByAsdIdAndNome(cmd.asdId(), cmd.nome())).thenReturn(false);
            when(venueRepository.save(any())).thenAnswer(inv -> {
                VenueEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(CreateVenueResult.Created.class);
            var created = (CreateVenueResult.Created) result;
            assertThat(created.venueId()).isNotNull();
            verify(venueRepository).save(any());
        }
    }

    @Nested
    @DisplayName("when a venue with the same nome already exists for the ASD")
    class WhenDuplicateName {

        @Test
        @DisplayName("returns DuplicateName without saving")
        void returnsDuplicateName() {
            var cmd = TestFixtures.createVenueCommandWithDuplicateName();
            when(venueRepository.existsByAsdIdAndNome(cmd.asdId(), cmd.nome())).thenReturn(true);

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(CreateVenueResult.DuplicateName.class);
            var duplicate = (CreateVenueResult.DuplicateName) result;
            assertThat(duplicate.nome()).isEqualTo(cmd.nome());
            verify(venueRepository, never()).save(any());
        }
    }
}
