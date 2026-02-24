package it.asd.compliance.shared.repository;

import it.asd.compliance.shared.entity.DocumentEntity;
import it.asd.compliance.shared.entity.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentEntity, UUID> {

    List<DocumentEntity> findByPersonIdAndAsdId(UUID personId, UUID asdId);

    @Query("""
           SELECT d FROM DocumentEntity d
           WHERE d.personId = :personId AND d.asdId = :asdId
             AND d.tipo = :tipo AND d.stato = 'VALID'
           ORDER BY d.dataScadenza DESC LIMIT 1
           """)
    Optional<DocumentEntity> findActiveByPersonAsdAndType(
            @Param("personId") UUID personId,
            @Param("asdId")    UUID asdId,
            @Param("tipo")     DocumentType tipo);

    @Query("""
           SELECT d FROM DocumentEntity d
           WHERE d.stato IN ('VALID', 'EXPIRING_SOON')
             AND d.dataScadenza <= :threshold
           """)
    List<DocumentEntity> findExpiringOrExpired(@Param("threshold") LocalDate threshold);
}
