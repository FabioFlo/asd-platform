package it.asd.registry.shared.repository;

import it.asd.registry.shared.entity.AsdEntity;
import it.asd.registry.shared.entity.AsdStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AsdRepository extends JpaRepository<AsdEntity, UUID> {
    Optional<AsdEntity> findByCodiceFiscale(String cf);
    List<AsdEntity> findByStato(AsdStatus stato);
}
