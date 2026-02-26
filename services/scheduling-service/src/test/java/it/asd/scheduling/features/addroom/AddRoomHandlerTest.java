package it.asd.scheduling.features.addroom;

import it.asd.scheduling.shared.TestFixtures;
import it.asd.scheduling.shared.entity.RoomEntity;
import it.asd.scheduling.shared.repository.RoomRepository;
import it.asd.scheduling.shared.repository.VenueRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddRoomHandler")
@Tag("unit")
class AddRoomHandlerTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private AddRoomHandler handler;

    @Nested
    @DisplayName("when venue exists and room name is unique")
    class WhenValid {

        @Test
        @DisplayName("returns Added and saves the room entity")
        void returnsAdded() {
            var cmd = TestFixtures.validAddRoomCommand();
            when(venueRepository.findById(cmd.venueId()))
                    .thenReturn(Optional.of(TestFixtures.savedVenue(cmd.venueId())));
            when(roomRepository.existsByVenueIdAndNome(cmd.venueId(), cmd.nome())).thenReturn(false);
            when(roomRepository.save(any())).thenAnswer(inv -> {
                RoomEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(AddRoomResult.Added.class);
            var added = (AddRoomResult.Added) result;
            assertThat(added.roomId()).isNotNull();
            verify(roomRepository).save(any());
        }
    }

    @Nested
    @DisplayName("when venue does not exist")
    class WhenVenueNotFound {

        @Test
        @DisplayName("returns VenueNotFound without saving any room")
        void returnsVenueNotFound() {
            var cmd = TestFixtures.validAddRoomCommand();
            when(venueRepository.findById(cmd.venueId())).thenReturn(Optional.empty());

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(AddRoomResult.VenueNotFound.class);
            var notFound = (AddRoomResult.VenueNotFound) result;
            assertThat(notFound.venueId()).isEqualTo(cmd.venueId());
            verify(roomRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("when a room with the same nome already exists in the venue")
    class WhenDuplicateName {

        @Test
        @DisplayName("returns DuplicateName without saving any room")
        void returnsDuplicateName() {
            var venueId = UUID.randomUUID();
            var cmd = TestFixtures.addRoomCommandWithDuplicateName(venueId);
            when(venueRepository.findById(venueId))
                    .thenReturn(Optional.of(TestFixtures.savedVenue(venueId)));
            when(roomRepository.existsByVenueIdAndNome(venueId, cmd.nome())).thenReturn(true);

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(AddRoomResult.DuplicateName.class);
            var duplicate = (AddRoomResult.DuplicateName) result;
            assertThat(duplicate.nome()).isEqualTo(cmd.nome());
            verify(roomRepository, never()).save(any());
        }
    }
}
