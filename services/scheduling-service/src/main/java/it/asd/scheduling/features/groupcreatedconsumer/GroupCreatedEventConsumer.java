package it.asd.scheduling.features.groupcreatedconsumer;

import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.events.membership.GroupCreatedEvent;
import it.asd.scheduling.shared.readmodel.GroupCacheEntity;
import it.asd.scheduling.shared.repository.GroupCacheRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class GroupCreatedEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(GroupCreatedEventConsumer.class);

    private final GroupCacheRepository groupCacheRepository;

    public GroupCreatedEventConsumer(GroupCacheRepository groupCacheRepository) {
        this.groupCacheRepository = groupCacheRepository;
    }

    @KafkaListener(topics = KafkaTopics.GROUP_CREATED)
    @Transactional
    public void consume(ConsumerRecord<String, EventEnvelope> record, Acknowledgment ack) {
        try {
            if (!(record.value().payload() instanceof GroupCreatedEvent evt)) {
                log.warn("[GROUP_CREATED_CONSUMER] Unexpected payload type, skipping");
                ack.acknowledge();
                return;
            }

            var cacheOpt = groupCacheRepository.findByGroupId(evt.groupId());
            if (cacheOpt.isPresent()) {
                var cache = cacheOpt.get();
                cache.setNome(evt.nome());
                cache.setDisciplina(evt.disciplina());
                cache.setTipo(evt.tipo());
                cache.setLastSyncedAt(LocalDateTime.now());
                groupCacheRepository.save(cache);
            } else {
                groupCacheRepository.save(GroupCacheEntity.builder()
                        .groupId(evt.groupId())
                        .asdId(evt.asdId())
                        .seasonId(evt.seasonId())
                        .nome(evt.nome())
                        .disciplina(evt.disciplina())
                        .tipo(evt.tipo())
                        .lastSyncedAt(LocalDateTime.now())
                        .build());
            }

            log.info("[GROUP_CREATED_CONSUMER] Upserted cache for groupId={}", evt.groupId());
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("[GROUP_CREATED_CONSUMER] Error processing event, not acking: {}", ex.getMessage());
        }
    }
}
