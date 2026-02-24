package it.asd.competition.features.recordresult;

import it.asd.common.kafka.EventPublisher;
import it.asd.competition.shared.repository.EventParticipationRepository;
import it.asd.events.KafkaTopics;
import it.asd.events.competition.ParticipantResultSetEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class RecordResultHandler {

    private static final Logger log = LoggerFactory.getLogger(RecordResultHandler.class);

    private final EventParticipationRepository repo;
    private final ParticipationMapper          mapper;
    private final EventPublisher               eventPublisher;

    public RecordResultHandler(EventParticipationRepository repo,
                               ParticipationMapper mapper,
                               EventPublisher eventPublisher) {
        this.repo          = repo;
        this.mapper        = mapper;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RecordResultResult handle(RecordResultCommand cmd) {
        var opt = repo.findById(cmd.participationId());
        if (opt.isEmpty())
            return new RecordResultResult.NotFound(cmd.participationId());

        var entity = opt.get();

        // MapStruct updates only non-null fields from the command
        mapper.updateFromCommand(cmd, entity);
        repo.save(entity);

        // Publish for sport satellite services (chess ELO, swim splits, etc.)
        eventPublisher.publish(
                KafkaTopics.PARTICIPANT_RESULT_SET,
                new ParticipantResultSetEvent(
                        UUID.randomUUID(), entity.getId(), entity.getEventId(),
                        entity.getPersonId(), entity.getGroupId(),
                        null,   // disciplina comes from the event — TODO: add to entity
                        entity.getPosizione(),
                        entity.getPunteggio() != null ? entity.getPunteggio().doubleValue() : null,
                        entity.getResultData(),
                        Instant.now()),
                entity.getAsdId(), entity.getSeasonId());

        log.info("[RECORD_RESULT] participationId={} posizione={}", entity.getId(), entity.getPosizione());

        return new RecordResultResult.Recorded(
                entity.getId(), entity.getPersonId(), entity.getGroupId(),
                null, entity.getPosizione(), entity.getPunteggio(), entity.getResultData());
    }
}
