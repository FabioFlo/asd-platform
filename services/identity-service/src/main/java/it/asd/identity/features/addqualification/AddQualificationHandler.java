package it.asd.identity.features.addqualification;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.events.identity.QualificationAddedEvent;
import it.asd.identity.shared.entity.QualificationEntity;
import it.asd.identity.shared.entity.QualificationStatus;
import it.asd.identity.shared.repository.PersonRepository;
import it.asd.identity.shared.repository.QualificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class AddQualificationHandler {

    private static final Logger log = LoggerFactory.getLogger(AddQualificationHandler.class);

    private final PersonRepository personRepository;
    private final QualificationRepository qualificationRepository;
    private final EventPublisher eventPublisher;

    public AddQualificationHandler(PersonRepository personRepository,
                                   QualificationRepository qualificationRepository,
                                   EventPublisher eventPublisher) {
        this.personRepository = personRepository;
        this.qualificationRepository = qualificationRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AddQualificationResult handle(AddQualificationCommand cmd) {
        if (personRepository.findById(cmd.personId()).isEmpty()) {
            return new AddQualificationResult.PersonNotFound(cmd.personId());
        }

        var entity = QualificationEntity.builder()
                .personId(cmd.personId())
                .tipo(cmd.tipo())
                .ente(cmd.ente())
                .livello(cmd.livello())
                .dataConseguimento(cmd.dataConseguimento())
                .dataScadenza(cmd.dataScadenza())
                .stato(QualificationStatus.VALID)
                .numeroPatentino(cmd.numeroPatentino())
                .note(cmd.note())
                .build();

        var saved = qualificationRepository.save(entity);
        log.info("[ADD_QUALIFICATION] Saved qualificationId={} personId={}", saved.getId(), saved.getPersonId());

        eventPublisher.publish(
                KafkaTopics.QUALIFICATION_ADDED,
                new QualificationAddedEvent(
                        UUID.randomUUID(), saved.getId(), saved.getPersonId(),
                        saved.getTipo(), saved.getEnte(), saved.getLivello(), Instant.now()),
                null, null);

        return new AddQualificationResult.Added(saved.getId());
    }
}
