package it.asd.membership.shared.repository;

import it.asd.membership.shared.entity.GroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<GroupEntity, UUID> {
    List<GroupEntity> findByAsdIdAndSeasonId(UUID asdId, UUID seasonId);

    boolean existsByAsdIdAndSeasonIdAndNome(UUID asdId, UUID seasonId, String nome);
}
