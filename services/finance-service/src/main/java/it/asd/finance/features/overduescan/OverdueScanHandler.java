package it.asd.finance.features.overduescan;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.events.finance.PaymentOverdueEvent;
import it.asd.finance.shared.entity.PaymentStatus;
import it.asd.finance.shared.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class OverdueScanHandler {

    private static final Logger log = LoggerFactory.getLogger(OverdueScanHandler.class);

    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;

    public OverdueScanHandler(PaymentRepository paymentRepository, EventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public OverdueScanResult scan() {
        var pending = paymentRepository.findByStatoAndDataScadenzaBefore(
                PaymentStatus.PENDING, LocalDate.now());

        int markedOverdue = 0;
        List<UUID> failed = new ArrayList<>();

        for (var payment : pending) {
            try {
                payment.setStato(PaymentStatus.OVERDUE);
                paymentRepository.save(payment);

                eventPublisher.publish(
                        KafkaTopics.PAYMENT_OVERDUE,
                        new PaymentOverdueEvent(
                                UUID.randomUUID(), payment.getId(), payment.getPersonId(),
                                payment.getAsdId(), payment.getImporto(), payment.getDataScadenza(),
                                Instant.now()),
                        payment.getAsdId(), payment.getSeasonId());

                markedOverdue++;
            } catch (Exception ex) {
                log.error("[OVERDUE_SCAN] Failed to mark paymentId={} as overdue: {}",
                        payment.getId(), ex.getMessage());
                failed.add(payment.getId());
            }
        }

        log.info("[OVERDUE_SCAN] Marked {} payments as overdue, {} failed", markedOverdue, failed.size());
        return new OverdueScanResult.Summary(markedOverdue, failed);
    }
}
