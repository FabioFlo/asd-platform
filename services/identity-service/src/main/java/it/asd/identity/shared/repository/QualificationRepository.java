package it.asd.identity.shared.repository;

import it.asd.identity.shared.entity.QualificationEntity;
import it.asd.identity.shared.entity.QualificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QualificationRepository extends JpaRepository<QualificationEntity, UUID> {
    List<QualificationEntity> findByPersonId(UUID personId);

    List<QualificationEntity> findByPersonIdAndTipoAndStato(
            UUID personId, String tipo, QualificationStatus stato);
}
