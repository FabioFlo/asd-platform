package it.asd.finance.features.membershipactivatedconsumer;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.events.finance.PaymentCreatedEvent;
import it.asd.events.membership.MembershipActivatedEvent;
import it.asd.finance.shared.entity.FeeRuleEntity;
import it.asd.finance.shared.entity.PaymentEntity;
import it.asd.finance.shared.entity.PaymentStatus;
import it.asd.finance.shared.entity.PaymentType;
import it.asd.finance.shared.repository.FeeRuleRepository;
import it.asd.finance.shared.repository.PaymentRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class MembershipActivatedConsumer {

    private static final Logger log = LoggerFactory.getLogger(MembershipActivatedConsumer.class);

    private final PaymentRepository paymentRepository;
    private final FeeRuleRepository feeRuleRepository;
    private final EventPublisher eventPublisher;
    private final BigDecimal defaultQuotaAssociativa;

    public MembershipActivatedConsumer(
            PaymentRepository paymentRepository,
            FeeRuleRepository feeRuleRepository,
            EventPublisher eventPublisher,
            @Value("${finance.defaults.quota-associativa}") BigDecimal defaultQuotaAssociativa) {
        this.paymentRepository = paymentRepository;
        this.feeRuleRepository = feeRuleRepository;
        this.eventPublisher = eventPublisher;
        this.defaultQuotaAssociativa = defaultQuotaAssociativa;
    }

    @KafkaListener(topics = KafkaTopics.MEMBERSHIP_ACTIVATED)
    @Transactional
    public void consume(ConsumerRecord<String, EventEnvelope> record, Acknowledgment ack) {
        try {
            if (!(record.value().payload() instanceof MembershipActivatedEvent evt)) {
                log.warn("[MEMBERSHIP_ACTIVATED_CONSUMER] Unexpected payload type, skipping");
                ack.acknowledge();
                return;
            }

            // Idempotency check: use membershipId as trigger key
            if (paymentRepository.existsByTriggerEventId(evt.membershipId())) {
                log.info("[MEMBERSHIP_ACTIVATED_CONSUMER] Already processed membershipId={}, skipping",
                        evt.membershipId());
                ack.acknowledge();
                return;
            }

            // Look up fee rule
            var feeRuleOpt = feeRuleRepository.findByAsdIdAndSeasonIdAndPaymentTypeAndAttivo(
                    evt.asdId(), evt.seasonId(), PaymentType.QUOTA_ASSOCIATIVA, true);

            BigDecimal importo = feeRuleOpt
                    .map(FeeRuleEntity::getImporto)
                    .orElse(defaultQuotaAssociativa);

            int giorniScadenza = feeRuleOpt
                    .map(FeeRuleEntity::getGiorniScadenza)
                    .orElse(30);

            LocalDate dataScadenza = evt.dataIscrizione().plusDays(giorniScadenza);

            var entity = PaymentEntity.builder()
                    .personId(evt.personId())
                    .asdId(evt.asdId())
                    .seasonId(evt.seasonId())
                    .triggerEventId(evt.membershipId())
                    .triggerType("membership.activated")
                    .paymentType(PaymentType.QUOTA_ASSOCIATIVA)
                    .importo(importo)
                    .dataScadenza(dataScadenza)
                    .stato(PaymentStatus.PENDING)
                    .build();

            var saved = paymentRepository.save(entity);
            log.info("[MEMBERSHIP_ACTIVATED_CONSUMER] Created paymentId={} for membershipId={}",
                    saved.getId(), evt.membershipId());

            eventPublisher.publish(
                    KafkaTopics.PAYMENT_CREATED,
                    new PaymentCreatedEvent(
                            UUID.randomUUID(), saved.getId(), saved.getPersonId(), saved.getAsdId(),
                            saved.getSeasonId(), saved.getPaymentType().name(), saved.getImporto(),
                            saved.getDataScadenza(), Instant.now()),
                    saved.getAsdId(), saved.getSeasonId());

            ack.acknowledge();
        } catch (Exception ex) {
            log.error("[MEMBERSHIP_ACTIVATED_CONSUMER] Error processing event, not acking: {}", ex.getMessage());
        }
    }
}
