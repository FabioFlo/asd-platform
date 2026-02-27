package it.asd.membership.features.personupdatedconsumer;

import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.events.identity.PersonUpdatedEvent;
import it.asd.membership.shared.readmodel.PersonCacheEntity;
import it.asd.membership.shared.repository.PersonCacheRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class PersonUpdatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PersonUpdatedEventConsumer.class);

    private final PersonCacheRepository personCacheRepository;

    public PersonUpdatedEventConsumer(PersonCacheRepository personCacheRepository) {
        this.personCacheRepository = personCacheRepository;
    }

    @KafkaListener(topics = KafkaTopics.PERSON_UPDATED)
    @Transactional
    public void consume(ConsumerRecord<String, EventEnvelope> record, Acknowledgment ack) {
        try {
            if (!(record.value().payload() instanceof PersonUpdatedEvent evt)) {
                log.warn("[PERSON_UPDATED_CONSUMER] Unexpected payload type, skipping");
                ack.acknowledge();
                return;
            }

            var cacheOpt = personCacheRepository.findByPersonId(evt.personId());
            if (cacheOpt.isPresent()) {
                var cache = cacheOpt.get();
                if (evt.nome() != null) cache.setNome(evt.nome());
                if (evt.cognome() != null) cache.setCognome(evt.cognome());
                if (evt.email() != null) cache.setEmail(evt.email());
                cache.setLastSyncedAt(LocalDateTime.now());
                cache.setSource("person.updated");
                personCacheRepository.save(cache);
            } else {
                personCacheRepository.save(PersonCacheEntity.builder()
                        .personId(evt.personId())
                        .nome(evt.nome())
                        .cognome(evt.cognome())
                        .email(evt.email())
                        .lastSyncedAt(LocalDateTime.now())
                        .source("person.updated")
                        .build());
            }

            log.info("[PERSON_UPDATED_CONSUMER] Upserted cache for personId={}", evt.personId());
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("[PERSON_UPDATED_CONSUMER] Error processing event, not acking: {}", ex.getMessage());
        }
    }
}
