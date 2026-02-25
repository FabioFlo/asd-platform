package it.asd.scheduling.features.schedulesession;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.events.scheduling.SessionScheduledEvent;
import it.asd.scheduling.shared.entity.SessionEntity;
import it.asd.scheduling.shared.entity.SessionStatus;
import it.asd.scheduling.shared.entity.VenueStatus;
import it.asd.scheduling.shared.repository.GroupCacheRepository;
import it.asd.scheduling.shared.repository.RoomRepository;
import it.asd.scheduling.shared.repository.SessionRepository;
import it.asd.scheduling.shared.repository.VenueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class ScheduleSessionHandler {

    private static final Logger log = LoggerFactory.getLogger(ScheduleSessionHandler.class);

    private final VenueRepository venueRepository;
    private final RoomRepository roomRepository;
    private final SessionRepository sessionRepository;
    private final GroupCacheRepository groupCacheRepository;
    private final EventPublisher eventPublisher;

    public ScheduleSessionHandler(VenueRepository venueRepository,
                                  RoomRepository roomRepository,
                                  SessionRepository sessionRepository,
                                  GroupCacheRepository groupCacheRepository,
                                  EventPublisher eventPublisher) {
        this.venueRepository = venueRepository;
        this.roomRepository = roomRepository;
        this.sessionRepository = sessionRepository;
        this.groupCacheRepository = groupCacheRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ScheduleSessionResult handle(ScheduleSessionCommand cmd) {
        // 1. Verify venue belongs to asdId
        var venueOpt = venueRepository.findById(cmd.venueId());
        if (venueOpt.isEmpty() || !venueOpt.get().getAsdId().equals(cmd.asdId())
                || venueOpt.get().getStato() != VenueStatus.ACTIVE) {
            return new ScheduleSessionResult.VenueNotFound();
        }

        // 2. If roomId provided, verify room belongs to venue
        if (cmd.roomId() != null) {
            var roomOpt = roomRepository.findById(cmd.roomId());
            if (roomOpt.isEmpty() || !roomOpt.get().getVenueId().equals(cmd.venueId())) {
                return new ScheduleSessionResult.RoomNotFound();
            }
        }

        // 3. Validate time range
        if (!cmd.oraFine().isAfter(cmd.oraInizio())) {
            return new ScheduleSessionResult.InvalidTimeRange();
        }

        // 4. If groupId provided, check group cache
        if (cmd.groupId() != null) {
            if (groupCacheRepository.findByGroupId(cmd.groupId()).isEmpty()) {
                return new ScheduleSessionResult.GroupNotFound();
            }
        }

        // 5. If roomId provided, check overlapping sessions
        if (cmd.roomId() != null) {
            var existingSessions = sessionRepository.findByRoomIdAndDataAndStato(
                    cmd.roomId(), cmd.data(), SessionStatus.SCHEDULED);
            for (var s : existingSessions) {
                boolean overlaps = cmd.oraInizio().isBefore(s.getOraFine())
                        && cmd.oraFine().isAfter(s.getOraInizio());
                if (overlaps) {
                    return new ScheduleSessionResult.TimeConflict(
                            "Room already has a session from " + s.getOraInizio() + " to " + s.getOraFine());
                }
            }
        }

        // 6. Save session
        var entity = SessionEntity.builder()
                .asdId(cmd.asdId())
                .venueId(cmd.venueId())
                .roomId(cmd.roomId())
                .groupId(cmd.groupId())
                .titolo(cmd.titolo())
                .data(cmd.data())
                .oraInizio(cmd.oraInizio())
                .oraFine(cmd.oraFine())
                .tipo(cmd.tipo())
                .stato(SessionStatus.SCHEDULED)
                .note(cmd.note())
                .build();

        var saved = sessionRepository.save(entity);
        log.info("[SCHEDULE_SESSION] Saved sessionId={} titolo={}", saved.getId(), saved.getTitolo());

        // 7. Publish event
        eventPublisher.publish(
                KafkaTopics.SESSION_SCHEDULED,
                new SessionScheduledEvent(
                        UUID.randomUUID(), saved.getId(), saved.getAsdId(), saved.getGroupId(),
                        saved.getVenueId(), saved.getData(), saved.getOraInizio(), saved.getOraFine(),
                        saved.getTipo().name(), Instant.now()),
                saved.getAsdId(), null);

        return new ScheduleSessionResult.Scheduled(saved.getId());
    }
}
