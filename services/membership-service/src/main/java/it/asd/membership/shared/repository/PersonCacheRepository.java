package it.asd.membership.shared.repository;

import it.asd.membership.shared.readmodel.PersonCacheEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PersonCacheRepository extends JpaRepository<PersonCacheEntity, UUID> {
    Optional<PersonCacheEntity> findByPersonId(UUID personId);
}
