package it.asd.membership.features.seasonactivatedconsumer;

import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.events.registry.SeasonActivatedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class SeasonActivatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SeasonActivatedEventConsumer.class);

    @KafkaListener(topics = KafkaTopics.SEASON_ACTIVATED)
    public void consume(ConsumerRecord<String, EventEnvelope> record, Acknowledgment ack) {
        try {
            if (!(record.value().payload() instanceof SeasonActivatedEvent evt)) {
                log.warn("[SEASON_ACTIVATED_CONSUMER] Unexpected payload type, skipping");
                ack.acknowledge();
                return;
            }
            log.info("[SEASON_ACTIVATED_CONSUMER] Season activated: asdId={} seasonId={} codice={}",
                    evt.asdId(), evt.seasonId(), evt.codice());
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("[SEASON_ACTIVATED_CONSUMER] Error processing event, not acking: {}", ex.getMessage());
        }
    }
}
