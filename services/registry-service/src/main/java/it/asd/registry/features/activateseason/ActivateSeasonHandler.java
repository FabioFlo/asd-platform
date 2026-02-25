package it.asd.registry.features.activateseason;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.events.registry.SeasonActivatedEvent;
import it.asd.registry.shared.entity.AsdStatus;
import it.asd.registry.shared.entity.SeasonEntity;
import it.asd.registry.shared.entity.SeasonStatus;
import it.asd.registry.shared.repository.AsdRepository;
import it.asd.registry.shared.repository.SeasonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class ActivateSeasonHandler {

    private static final Logger log = LoggerFactory.getLogger(ActivateSeasonHandler.class);

    private final AsdRepository asdRepository;
    private final SeasonRepository seasonRepository;
    private final EventPublisher eventPublisher;

    public ActivateSeasonHandler(AsdRepository asdRepository,
                                 SeasonRepository seasonRepository,
                                 EventPublisher eventPublisher) {
        this.asdRepository = asdRepository;
        this.seasonRepository = seasonRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public ActivateSeasonResult handle(ActivateSeasonCommand cmd) {
        var asdOpt = asdRepository.findById(cmd.asdId());
        if (asdOpt.isEmpty() || asdOpt.get().getStato() != AsdStatus.ACTIVE) {
            return new ActivateSeasonResult.AsdNotFound(cmd.asdId());
        }

        if (!cmd.dataFine().isAfter(cmd.dataInizio())) {
            return new ActivateSeasonResult.InvalidDateRange("dataFine must be after dataInizio");
        }

        if (seasonRepository.existsByAsdIdAndCodice(cmd.asdId(), cmd.codice())) {
            return new ActivateSeasonResult.DuplicateCodice(cmd.codice());
        }

        var activeOpt = seasonRepository.findByAsdIdAndStato(cmd.asdId(), SeasonStatus.ACTIVE);
        if (activeOpt.isPresent()) {
            return new ActivateSeasonResult.AlreadyHasActiveSeason(activeOpt.get().getCodice());
        }

        var entity = SeasonEntity.builder()
                .asdId(cmd.asdId())
                .codice(cmd.codice())
                .dataInizio(cmd.dataInizio())
                .dataFine(cmd.dataFine())
                .stato(SeasonStatus.ACTIVE)
                .build();

        var saved = seasonRepository.save(entity);
        log.info("[ACTIVATE_SEASON] Saved seasonId={} codice={} asdId={}",
                saved.getId(), saved.getCodice(), saved.getAsdId());

        eventPublisher.publish(
                KafkaTopics.SEASON_ACTIVATED,
                new SeasonActivatedEvent(
                        UUID.randomUUID(), saved.getAsdId(), saved.getId(),
                        saved.getCodice(), Instant.now()),
                saved.getAsdId(), saved.getId());

        return new ActivateSeasonResult.Activated(saved.getId(), saved.getCodice());
    }
}
