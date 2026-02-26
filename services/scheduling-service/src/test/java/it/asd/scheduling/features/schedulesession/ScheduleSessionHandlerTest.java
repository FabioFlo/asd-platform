package it.asd.scheduling.features.schedulesession;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.scheduling.shared.TestFixtures;
import it.asd.scheduling.shared.entity.SessionEntity;
import it.asd.scheduling.shared.entity.SessionStatus;
import it.asd.scheduling.shared.repository.GroupCacheRepository;
import it.asd.scheduling.shared.repository.RoomRepository;
import it.asd.scheduling.shared.repository.SessionRepository;
import it.asd.scheduling.shared.repository.VenueRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleSessionHandler")
@Tag("unit")
class ScheduleSessionHandlerTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private GroupCacheRepository groupCacheRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private ScheduleSessionHandler handler;

    @Nested
    @DisplayName("when all inputs are valid and no room is specified")
    class WhenValidWithoutRoom {

        @Test
        @DisplayName("returns Scheduled, saves entity, and publishes SESSION_SCHEDULED event")
        void returnsScheduled() {
            var cmd = TestFixtures.validScheduleSessionCommand();
            when(venueRepository.findById(cmd.venueId()))
                    .thenReturn(Optional.of(TestFixtures.savedVenue(cmd.venueId())));
            when(sessionRepository.save(any())).thenAnswer(inv -> {
                SessionEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ScheduleSessionResult.Scheduled.class);
            var scheduled = (ScheduleSessionResult.Scheduled) result;
            assertThat(scheduled.sessionId()).isNotNull();
            verify(sessionRepository).save(any());
            verify(eventPublisher).publish(eq(KafkaTopics.SESSION_SCHEDULED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when all inputs are valid, room and group are specified")
    class WhenValidWithRoomAndGroup {

        @Test
        @DisplayName("returns Scheduled, verifies room + group, saves entity, and publishes event")
        void returnsScheduledWithRoomAndGroup() {
            var roomId  = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var cmd = TestFixtures.validScheduleSessionCommand(TestFixtures.VENUE_ID, roomId, groupId);
            when(venueRepository.findById(cmd.venueId()))
                    .thenReturn(Optional.of(TestFixtures.savedVenue(cmd.venueId())));
            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(TestFixtures.savedRoom(roomId, cmd.venueId())));
            when(groupCacheRepository.findByGroupId(groupId))
                    .thenReturn(Optional.of(TestFixtures.savedGroupCache(groupId)));
            when(sessionRepository.findByRoomIdAndDataAndStato(
                    eq(roomId), eq(cmd.data()), eq(SessionStatus.SCHEDULED)))
                    .thenReturn(List.of());
            when(sessionRepository.save(any())).thenAnswer(inv -> {
                SessionEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ScheduleSessionResult.Scheduled.class);
            verify(sessionRepository).save(any());
            verify(eventPublisher).publish(eq(KafkaTopics.SESSION_SCHEDULED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when venue does not exist or does not belong to the ASD")
    class WhenVenueNotFound {

        @Test
        @DisplayName("returns VenueNotFound without saving or publishing")
        void returnsVenueNotFound() {
            var cmd = TestFixtures.validScheduleSessionCommand();
            when(venueRepository.findById(cmd.venueId())).thenReturn(Optional.empty());

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ScheduleSessionResult.VenueNotFound.class);
            verify(sessionRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("returns VenueNotFound when venue belongs to a different ASD")
        void returnsVenueNotFoundWhenWrongAsd() {
            var cmd = TestFixtures.validScheduleSessionCommand();
            var venueWithDifferentAsd = TestFixtures.savedVenue(cmd.venueId());
            venueWithDifferentAsd.setAsdId(UUID.randomUUID()); // different ASD
            when(venueRepository.findById(cmd.venueId())).thenReturn(Optional.of(venueWithDifferentAsd));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ScheduleSessionResult.VenueNotFound.class);
            verify(sessionRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when roomId is provided but room does not exist or belongs to a different venue")
    class WhenRoomNotFound {

        @Test
        @DisplayName("returns RoomNotFound without saving or publishing")
        void returnsRoomNotFound() {
            var roomId = UUID.randomUUID();
            var cmd = TestFixtures.validScheduleSessionCommand(TestFixtures.VENUE_ID, roomId, null);
            when(venueRepository.findById(cmd.venueId()))
                    .thenReturn(Optional.of(TestFixtures.savedVenue(cmd.venueId())));
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ScheduleSessionResult.RoomNotFound.class);
            verify(sessionRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("returns RoomNotFound when room belongs to a different venue")
        void returnsRoomNotFoundWhenWrongVenue() {
            var roomId    = UUID.randomUUID();
            var otherVenueId = UUID.randomUUID();
            var cmd = TestFixtures.validScheduleSessionCommand(TestFixtures.VENUE_ID, roomId, null);
            when(venueRepository.findById(cmd.venueId()))
                    .thenReturn(Optional.of(TestFixtures.savedVenue(cmd.venueId())));
            // Room belongs to a different venue
            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(TestFixtures.savedRoom(roomId, otherVenueId)));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ScheduleSessionResult.RoomNotFound.class);
            verify(sessionRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when groupId is provided but not found in the cache")
    class WhenGroupNotFound {

        @Test
        @DisplayName("returns GroupNotFound without saving or publishing")
        void returnsGroupNotFound() {
            var groupId = UUID.randomUUID();
            var cmd = TestFixtures.validScheduleSessionCommand(TestFixtures.VENUE_ID, null, groupId);
            when(venueRepository.findById(cmd.venueId()))
                    .thenReturn(Optional.of(TestFixtures.savedVenue(cmd.venueId())));
            when(groupCacheRepository.findByGroupId(groupId)).thenReturn(Optional.empty());

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ScheduleSessionResult.GroupNotFound.class);
            verify(sessionRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when oraFine is not after oraInizio")
    class WhenInvalidTimeRange {

        @Test
        @DisplayName("returns InvalidTimeRange without saving or publishing")
        void returnsInvalidTimeRange() {
            var cmd = TestFixtures.scheduleSessionCommandWithInvalidTimeRange(TestFixtures.VENUE_ID);
            when(venueRepository.findById(cmd.venueId()))
                    .thenReturn(Optional.of(TestFixtures.savedVenue(cmd.venueId())));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ScheduleSessionResult.InvalidTimeRange.class);
            verify(sessionRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when room has an overlapping SCHEDULED session on the same day")
    class WhenTimeConflict {

        @Test
        @DisplayName("returns TimeConflict without saving or publishing")
        void returnsTimeConflict() {
            var roomId = UUID.randomUUID();
            var cmd = TestFixtures.validScheduleSessionCommand(TestFixtures.VENUE_ID, roomId, null);
            when(venueRepository.findById(cmd.venueId()))
                    .thenReturn(Optional.of(TestFixtures.savedVenue(cmd.venueId())));
            when(roomRepository.findById(roomId))
                    .thenReturn(Optional.of(TestFixtures.savedRoom(roomId, cmd.venueId())));
            // Existing session occupies 09:00–11:00, which overlaps with 10:00–12:00
            var conflicting = TestFixtures.conflictingSession(
                    roomId, cmd.data(), LocalTime.of(9, 0), LocalTime.of(11, 0));
            when(sessionRepository.findByRoomIdAndDataAndStato(
                    eq(roomId), eq(cmd.data()), eq(SessionStatus.SCHEDULED)))
                    .thenReturn(List.of(conflicting));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(ScheduleSessionResult.TimeConflict.class);
            verify(sessionRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }
}
