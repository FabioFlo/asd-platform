package it.asd.identity.shared.repository;

import it.asd.identity.shared.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<PersonEntity, UUID> {
    Optional<PersonEntity> findByCodiceFiscale(String cf);

    Optional<PersonEntity> findByEmail(String email);
}
