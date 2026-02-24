package it.asd.competition.shared.repository;

import it.asd.competition.shared.entity.EventParticipationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EventParticipationRepository
        extends JpaRepository<EventParticipationEntity, UUID> {

    List<EventParticipationEntity> findByPersonIdAndAsdId(UUID personId, UUID asdId);
    List<EventParticipationEntity> findByEventId(UUID eventId);
}
