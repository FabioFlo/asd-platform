package it.asd.scheduling.shared.repository;

import it.asd.scheduling.shared.entity.VenueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface VenueRepository extends JpaRepository<VenueEntity, UUID> {
    List<VenueEntity> findByAsdId(UUID asdId);
    boolean existsByAsdIdAndNome(UUID asdId, String nome);
}
