package it.asd.finance.features.groupenrollmentaddedconsumer;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.events.finance.PaymentCreatedEvent;
import it.asd.events.membership.GroupEnrollmentAddedEvent;
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
public class GroupEnrollmentAddedConsumer {

    private static final Logger log = LoggerFactory.getLogger(GroupEnrollmentAddedConsumer.class);

    private final PaymentRepository paymentRepository;
    private final FeeRuleRepository feeRuleRepository;
    private final EventPublisher eventPublisher;
    private final BigDecimal defaultQuotaCorso;

    public GroupEnrollmentAddedConsumer(
            PaymentRepository paymentRepository,
            FeeRuleRepository feeRuleRepository,
            EventPublisher eventPublisher,
            @Value("${finance.defaults.quota-corso}") BigDecimal defaultQuotaCorso) {
        this.paymentRepository = paymentRepository;
        this.feeRuleRepository = feeRuleRepository;
        this.eventPublisher = eventPublisher;
        this.defaultQuotaCorso = defaultQuotaCorso;
    }

    @KafkaListener(topics = KafkaTopics.GROUP_ENROLLMENT_ADDED)
    @Transactional
    public void consume(ConsumerRecord<String, EventEnvelope> record, Acknowledgment ack) {
        try {
            if (!(record.value().payload() instanceof GroupEnrollmentAddedEvent evt)) {
                log.warn("[GROUP_ENROLLMENT_ADDED_CONSUMER] Unexpected payload type, skipping");
                ack.acknowledge();
                return;
            }

            // Idempotency check
            if (paymentRepository.existsByTriggerEventId(evt.enrollmentId())) {
                log.info("[GROUP_ENROLLMENT_ADDED_CONSUMER] Already processed enrollmentId={}, skipping",
                        evt.enrollmentId());
                ack.acknowledge();
                return;
            }

            var feeRuleOpt = feeRuleRepository.findByAsdIdAndSeasonIdAndPaymentTypeAndAttivo(
                    evt.asdId(), evt.seasonId(), PaymentType.QUOTA_CORSO, true);

            BigDecimal importo = feeRuleOpt.map(FeeRuleEntity::getImporto).orElse(defaultQuotaCorso);
            int giorniScadenza = feeRuleOpt.map(FeeRuleEntity::getGiorniScadenza).orElse(30);
            LocalDate dataScadenza = evt.dataIngresso().plusDays(giorniScadenza);

            var entity = PaymentEntity.builder()
                    .personId(evt.personId())
                    .asdId(evt.asdId())
                    .seasonId(evt.seasonId())
                    .triggerEventId(evt.enrollmentId())
                    .triggerType("group.enrollment.added")
                    .paymentType(PaymentType.QUOTA_CORSO)
                    .importo(importo)
                    .dataScadenza(dataScadenza)
                    .stato(PaymentStatus.PENDING)
                    .build();

            var saved = paymentRepository.save(entity);
            log.info("[GROUP_ENROLLMENT_ADDED_CONSUMER] Created paymentId={} for enrollmentId={}",
                    saved.getId(), evt.enrollmentId());

            eventPublisher.publish(
                    KafkaTopics.PAYMENT_CREATED,
                    new PaymentCreatedEvent(
                            UUID.randomUUID(), saved.getId(), saved.getPersonId(), saved.getAsdId(),
                            saved.getSeasonId(), saved.getPaymentType().name(), saved.getImporto(),
                            saved.getDataScadenza(), Instant.now()),
                    saved.getAsdId(), saved.getSeasonId());

            ack.acknowledge();
        } catch (Exception ex) {
            log.error("[GROUP_ENROLLMENT_ADDED_CONSUMER] Error processing event, not acking: {}", ex.getMessage());
        }
    }
}
