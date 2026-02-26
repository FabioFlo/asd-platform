package it.asd.scheduling.shared;

import it.asd.scheduling.features.addroom.AddRoomCommand;
import it.asd.scheduling.features.createvenue.CreateVenueCommand;
import it.asd.scheduling.features.schedulesession.ScheduleSessionCommand;
import it.asd.scheduling.shared.entity.RoomEntity;
import it.asd.scheduling.shared.entity.RoomStatus;
import it.asd.scheduling.shared.entity.SessionEntity;
import it.asd.scheduling.shared.entity.SessionStatus;
import it.asd.scheduling.shared.entity.SessionType;
import it.asd.scheduling.shared.entity.VenueEntity;
import it.asd.scheduling.shared.entity.VenueStatus;
import it.asd.scheduling.shared.readmodel.GroupCacheEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Centralized test data for scheduling-service.
 * All test classes import from here — never build entities inline.
 */
public final class TestFixtures {

    private TestFixtures() {}

    // ── Shared IDs ────────────────────────────────────────────────────────────

    public static final UUID ASD_ID    = UUID.fromString("00000000-0000-0000-0000-000000000001");
    public static final UUID VENUE_ID  = UUID.fromString("00000000-0000-0000-0000-000000000002");
    public static final UUID ROOM_ID   = UUID.fromString("00000000-0000-0000-0000-000000000003");
    public static final UUID GROUP_ID  = UUID.fromString("00000000-0000-0000-0000-000000000004");
    public static final UUID SEASON_ID = UUID.fromString("00000000-0000-0000-0000-000000000005");

    // ── Commands ──────────────────────────────────────────────────────────────

    public static CreateVenueCommand validCreateVenueCommand() {
        return new CreateVenueCommand(
                ASD_ID,
                "Palazzetto dello Sport",
                "Via dello Sport 1",
                "Milano",
                "MI",
                null
        );
    }

    public static CreateVenueCommand createVenueCommandWithDuplicateName() {
        return new CreateVenueCommand(
                ASD_ID,
                "Palazzetto dello Sport",   // same nome as above
                "Via Altra 99",
                "Milano",
                "MI",
                null
        );
    }

    public static AddRoomCommand validAddRoomCommand() {
        return validAddRoomCommand(VENUE_ID);
    }

    public static AddRoomCommand validAddRoomCommand(UUID venueId) {
        return new AddRoomCommand(
                venueId,
                "Sala Principale",
                50,
                null
        );
    }

    public static AddRoomCommand addRoomCommandWithDuplicateName(UUID venueId) {
        return new AddRoomCommand(
                venueId,
                "Sala Principale",  // same nome
                30,
                null
        );
    }

    public static ScheduleSessionCommand validScheduleSessionCommand() {
        return validScheduleSessionCommand(VENUE_ID, null, null);
    }

    public static ScheduleSessionCommand validScheduleSessionCommand(UUID venueId, UUID roomId, UUID groupId) {
        return new ScheduleSessionCommand(
                ASD_ID,
                venueId,
                roomId,
                groupId,
                "Allenamento Squadra A",
                LocalDate.of(2026, 6, 15),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                SessionType.TRAINING,
                null
        );
    }

    public static ScheduleSessionCommand scheduleSessionCommandWithInvalidTimeRange(UUID venueId) {
        return new ScheduleSessionCommand(
                ASD_ID,
                venueId,
                null,
                null,
                "Sessione Invalida",
                LocalDate.of(2026, 6, 15),
                LocalTime.of(12, 0),    // oraInizio AFTER oraFine
                LocalTime.of(10, 0),
                SessionType.TRAINING,
                null
        );
    }

    // ── Entities (for repository stubs in unit tests) ─────────────────────────

    public static VenueEntity savedVenue(UUID id) {
        return VenueEntity.builder()
                .id(id)
                .asdId(ASD_ID)
                .nome("Palazzetto dello Sport")
                .indirizzo("Via dello Sport 1")
                .citta("Milano")
                .provincia("MI")
                .stato(VenueStatus.ACTIVE)
                .build();
    }

    public static VenueEntity savedVenue() {
        return savedVenue(VENUE_ID);
    }

    public static RoomEntity savedRoom(UUID id, UUID venueId) {
        return RoomEntity.builder()
                .id(id)
                .venueId(venueId)
                .nome("Sala Principale")
                .capienza(50)
                .stato(RoomStatus.ACTIVE)
                .build();
    }

    public static RoomEntity savedRoom() {
        return savedRoom(ROOM_ID, VENUE_ID);
    }

    public static SessionEntity savedSession(UUID id, UUID venueId, UUID roomId) {
        return SessionEntity.builder()
                .id(id)
                .asdId(ASD_ID)
                .venueId(venueId)
                .roomId(roomId)
                .titolo("Allenamento Squadra A")
                .data(LocalDate.of(2026, 6, 15))
                .oraInizio(LocalTime.of(10, 0))
                .oraFine(LocalTime.of(12, 0))
                .tipo(SessionType.TRAINING)
                .stato(SessionStatus.SCHEDULED)
                .build();
    }

    public static SessionEntity savedSession() {
        return savedSession(UUID.randomUUID(), VENUE_ID, ROOM_ID);
    }

    /** A conflicting session occupying the same room and time slot. */
    public static SessionEntity conflictingSession(UUID roomId, LocalDate data,
                                                   LocalTime oraInizio, LocalTime oraFine) {
        return SessionEntity.builder()
                .id(UUID.randomUUID())
                .asdId(ASD_ID)
                .venueId(VENUE_ID)
                .roomId(roomId)
                .titolo("Session Already Booked")
                .data(data)
                .oraInizio(oraInizio)
                .oraFine(oraFine)
                .tipo(SessionType.MATCH)
                .stato(SessionStatus.SCHEDULED)
                .build();
    }

    public static GroupCacheEntity savedGroupCache(UUID groupId) {
        return GroupCacheEntity.builder()
                .id(UUID.randomUUID())
                .groupId(groupId)
                .asdId(ASD_ID)
                .seasonId(SEASON_ID)
                .nome("Under 14")
                .disciplina("Nuoto")
                .tipo("AGONISTICO")
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }
}
