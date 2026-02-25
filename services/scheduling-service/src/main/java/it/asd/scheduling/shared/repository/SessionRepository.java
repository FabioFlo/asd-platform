package it.asd.scheduling.shared.repository;

import it.asd.scheduling.shared.entity.SessionEntity;
import it.asd.scheduling.shared.entity.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {
    List<SessionEntity> findByRoomIdAndDataAndStato(UUID roomId, LocalDate data, SessionStatus stato);
}
