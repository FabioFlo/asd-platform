package it.asd.finance.features.confirmpayment;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.events.finance.PaymentConfirmedEvent;
import it.asd.finance.shared.entity.PaymentStatus;
import it.asd.finance.shared.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class ConfirmPaymentHandler {

    private static final Logger log = LoggerFactory.getLogger(ConfirmPaymentHandler.class);

    private final PaymentRepository paymentRepository;
    private final EventPublisher eventPublisher;

    public ConfirmPaymentHandler(PaymentRepository paymentRepository, EventPublisher eventPublisher) {
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ConfirmPaymentResult handle(ConfirmPaymentCommand cmd) {
        var opt = paymentRepository.findById(cmd.paymentId());
        if (opt.isEmpty()) {
            return new ConfirmPaymentResult.NotFound(cmd.paymentId());
        }

        var entity = opt.get();

        if (entity.getStato() == PaymentStatus.CONFIRMED) {
            return new ConfirmPaymentResult.AlreadyConfirmed();
        }
        if (entity.getStato() == PaymentStatus.CANCELLED) {
            return new ConfirmPaymentResult.AlreadyCancelled();
        }

        entity.setStato(PaymentStatus.CONFIRMED);
        entity.setDataPagamento(cmd.dataPagamento());
        entity.setMetodoPagamento(cmd.metodoPagamento());
        entity.setRiferimento(cmd.riferimento());
        if (cmd.note() != null) entity.setNote(cmd.note());

        var saved = paymentRepository.save(entity);
        log.info("[CONFIRM_PAYMENT] Confirmed paymentId={}", saved.getId());

        eventPublisher.publish(
                KafkaTopics.PAYMENT_CONFIRMED,
                new PaymentConfirmedEvent(
                        UUID.randomUUID(), saved.getId(), saved.getPersonId(), saved.getAsdId(),
                        saved.getImporto(), saved.getDataPagamento(), Instant.now()),
                saved.getAsdId(), saved.getSeasonId());

        return new ConfirmPaymentResult.Confirmed(saved.getId(), saved.getImporto());
    }
}
