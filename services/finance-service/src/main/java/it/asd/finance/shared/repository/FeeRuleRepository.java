package it.asd.finance.shared.repository;

import it.asd.finance.shared.entity.FeeRuleEntity;
import it.asd.finance.shared.entity.PaymentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FeeRuleRepository extends JpaRepository<FeeRuleEntity, UUID> {
    Optional<FeeRuleEntity> findByAsdIdAndSeasonIdAndPaymentTypeAndAttivo(
            UUID asdId, UUID seasonId, PaymentType type, boolean attivo);
}
