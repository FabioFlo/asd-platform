package it.asd.competition.shared.repository;

import it.asd.competition.shared.entity.EligibilityCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EligibilityCacheRepository
        extends JpaRepository<EligibilityCacheEntity, UUID> {

    Optional<EligibilityCacheEntity> findByPersonIdAndAsdId(UUID personId, UUID asdId);
}
