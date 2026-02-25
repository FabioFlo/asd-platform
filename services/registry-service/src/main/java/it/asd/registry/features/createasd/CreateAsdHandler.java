package it.asd.registry.features.createasd;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.events.registry.AsdCreatedEvent;
import it.asd.registry.shared.entity.AsdEntity;
import it.asd.registry.shared.entity.AsdStatus;
import it.asd.registry.shared.repository.AsdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class CreateAsdHandler {

    private static final Logger log = LoggerFactory.getLogger(CreateAsdHandler.class);

    private final AsdRepository asdRepository;
    private final EventPublisher eventPublisher;

    public CreateAsdHandler(AsdRepository asdRepository, EventPublisher eventPublisher) {
        this.asdRepository = asdRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CreateAsdResult handle(CreateAsdCommand cmd) {
        if (asdRepository.findByCodiceFiscale(cmd.codiceFiscale()).isPresent()) {
            return new CreateAsdResult.DuplicateCodiceFiscale(cmd.codiceFiscale());
        }

        var entity = AsdEntity.builder()
                .codiceFiscale(cmd.codiceFiscale())
                .nome(cmd.nome())
                .codiceAffiliazioneConi(cmd.codiceAffiliazioneConi())
                .codiceAffiliazioneFsn(cmd.codiceAffiliazioneFsn())
                .disciplina(cmd.disciplina())
                .citta(cmd.citta())
                .provincia(cmd.provincia())
                .email(cmd.email())
                .telefono(cmd.telefono())
                .stato(AsdStatus.ACTIVE)
                .build();

        var saved = asdRepository.save(entity);
        log.info("[CREATE_ASD] Saved asdId={} cf={}", saved.getId(), saved.getCodiceFiscale());

        eventPublisher.publish(
                KafkaTopics.ASD_CREATED,
                new AsdCreatedEvent(
                        UUID.randomUUID(), saved.getId(), saved.getCodiceFiscale(),
                        saved.getNome(), saved.getDisciplina(), Instant.now()),
                saved.getId(), null);

        return new CreateAsdResult.Created(saved.getId(), saved.getNome());
    }
}
