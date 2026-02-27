package it.asd.membership.shared.repository;

import it.asd.membership.shared.entity.MembershipEntity;
import it.asd.membership.shared.entity.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MembershipRepository extends JpaRepository<MembershipEntity, UUID> {
    Optional<MembershipEntity> findByPersonIdAndAsdIdAndSeasonId(UUID personId, UUID asdId, UUID seasonId);

    List<MembershipEntity> findByPersonIdAndAsdIdAndStato(UUID personId, UUID asdId, MembershipStatus stato);

    Optional<MembershipEntity> findByNumeroTessera(String numero);
}
