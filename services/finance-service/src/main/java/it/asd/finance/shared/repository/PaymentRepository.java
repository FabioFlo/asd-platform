package it.asd.finance.shared.repository;

import it.asd.finance.shared.entity.PaymentEntity;
import it.asd.finance.shared.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<PaymentEntity, UUID> {
    List<PaymentEntity> findByPersonIdAndAsdId(UUID personId, UUID asdId);
    List<PaymentEntity> findByStatoAndDataScadenzaBefore(PaymentStatus stato, LocalDate date);
    boolean existsByTriggerEventId(UUID triggerEventId);
}
