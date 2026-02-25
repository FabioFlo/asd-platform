package it.asd.membership.shared.repository;

import it.asd.membership.shared.entity.EnrollmentStatus;
import it.asd.membership.shared.entity.GroupEnrollmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupEnrollmentRepository extends JpaRepository<GroupEnrollmentEntity, UUID> {
    Optional<GroupEnrollmentEntity> findByPersonIdAndGroupIdAndSeasonId(UUID personId, UUID groupId, UUID seasonId);
    List<GroupEnrollmentEntity> findByGroupIdAndSeasonIdAndStato(UUID groupId, UUID seasonId, EnrollmentStatus stato);
}
