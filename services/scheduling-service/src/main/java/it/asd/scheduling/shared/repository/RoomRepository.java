package it.asd.scheduling.shared.repository;

import it.asd.scheduling.shared.entity.RoomEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<RoomEntity, UUID> {
    List<RoomEntity> findByVenueId(UUID venueId);

    boolean existsByVenueIdAndNome(UUID venueId, String nome);
}
