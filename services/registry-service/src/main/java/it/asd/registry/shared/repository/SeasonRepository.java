package it.asd.registry.shared.repository;

import it.asd.registry.shared.entity.SeasonEntity;
import it.asd.registry.shared.entity.SeasonStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeasonRepository extends JpaRepository<SeasonEntity, UUID> {
    Optional<SeasonEntity> findByAsdIdAndStato(UUID asdId, SeasonStatus stato);

    List<SeasonEntity> findByAsdIdOrderByDataInizioDesc(UUID asdId);

    boolean existsByAsdIdAndCodice(UUID asdId, String codice);
}
