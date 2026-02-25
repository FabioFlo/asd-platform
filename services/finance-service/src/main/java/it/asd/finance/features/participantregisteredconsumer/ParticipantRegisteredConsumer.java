package it.asd.finance.features.participantregisteredconsumer;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.events.competition.ParticipantRegisteredEvent;
import it.asd.events.finance.PaymentCreatedEvent;
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
public class ParticipantRegisteredConsumer {

    private static final Logger log = LoggerFactory.getLogger(ParticipantRegisteredConsumer.class);

    private final PaymentRepository paymentRepository;
    private final FeeRuleRepository feeRuleRepository;
    private final EventPublisher eventPublisher;
    private final BigDecimal defaultIscrizioneGara;

    public ParticipantRegisteredConsumer(
            PaymentRepository paymentRepository,
            FeeRuleRepository feeRuleRepository,
            EventPublisher eventPublisher,
            @Value("${finance.defaults.iscrizione-gara}") BigDecimal defaultIscrizioneGara) {
        this.paymentRepository = paymentRepository;
        this.feeRuleRepository = feeRuleRepository;
        this.eventPublisher = eventPublisher;
        this.defaultIscrizioneGara = defaultIscrizioneGara;
    }

    @KafkaListener(topics = KafkaTopics.PARTICIPANT_REGISTERED)
    @Transactional
    public void consume(ConsumerRecord<String, EventEnvelope> record, Acknowledgment ack) {
        try {
            if (!(record.value().payload() instanceof ParticipantRegisteredEvent evt)) {
                log.warn("[PARTICIPANT_REGISTERED_CONSUMER] Unexpected payload type, skipping");
                ack.acknowledge();
                return;
            }

            // Idempotency check: use participationId as trigger key
            if (paymentRepository.existsByTriggerEventId(evt.participationId())) {
                log.info("[PARTICIPANT_REGISTERED_CONSUMER] Already processed participationId={}, skipping",
                        evt.participationId());
                ack.acknowledge();
                return;
            }

            var feeRuleOpt = feeRuleRepository.findByAsdIdAndSeasonIdAndPaymentTypeAndAttivo(
                    evt.asdId(), evt.seasonId(), PaymentType.ISCRIZIONE_GARA, true);

            BigDecimal importo = feeRuleOpt.map(FeeRuleEntity::getImporto).orElse(defaultIscrizioneGara);
            int giorniScadenza = feeRuleOpt.map(FeeRuleEntity::getGiorniScadenza).orElse(30);
            LocalDate dataScadenza = LocalDate.now().plusDays(giorniScadenza);

            var entity = PaymentEntity.builder()
                    .personId(evt.personId())
                    .asdId(evt.asdId())
                    .seasonId(evt.seasonId())
                    .triggerEventId(evt.participationId())
                    .triggerType("competition.participant.registered")
                    .paymentType(PaymentType.ISCRIZIONE_GARA)
                    .importo(importo)
                    .dataScadenza(dataScadenza)
                    .stato(PaymentStatus.PENDING)
                    .build();

            var saved = paymentRepository.save(entity);
            log.info("[PARTICIPANT_REGISTERED_CONSUMER] Created paymentId={} for participationId={}",
                    saved.getId(), evt.participationId());

            eventPublisher.publish(
                    KafkaTopics.PAYMENT_CREATED,
                    new PaymentCreatedEvent(
                            UUID.randomUUID(), saved.getId(), saved.getPersonId(), saved.getAsdId(),
                            saved.getSeasonId(), saved.getPaymentType().name(), saved.getImporto(),
                            saved.getDataScadenza(), Instant.now()),
                    saved.getAsdId(), saved.getSeasonId());

            ack.acknowledge();
        } catch (Exception ex) {
            log.error("[PARTICIPANT_REGISTERED_CONSUMER] Error processing event, not acking: {}", ex.getMessage());
        }
    }
}
