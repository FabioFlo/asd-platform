package it.asd.identity.features.updateperson;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.events.identity.PersonUpdatedEvent;
import it.asd.identity.shared.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class UpdatePersonHandler {

    private static final Logger log = LoggerFactory.getLogger(UpdatePersonHandler.class);

    private final PersonRepository personRepository;
    private final EventPublisher eventPublisher;

    public UpdatePersonHandler(PersonRepository personRepository, EventPublisher eventPublisher) {
        this.personRepository = personRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public UpdatePersonResult handle(UpdatePersonCommand cmd) {
        var opt = personRepository.findById(cmd.personId());
        if (opt.isEmpty()) {
            return new UpdatePersonResult.NotFound(cmd.personId());
        }

        var entity = opt.get();

        if (cmd.email() != null && !cmd.email().equals(entity.getEmail())) {
            if (personRepository.findByEmail(cmd.email()).isPresent()) {
                return new UpdatePersonResult.DuplicateEmail(cmd.email());
            }
        }

        if (cmd.nome() != null) entity.setNome(cmd.nome());
        if (cmd.cognome() != null) entity.setCognome(cmd.cognome());
        if (cmd.email() != null) entity.setEmail(cmd.email());
        if (cmd.telefono() != null) entity.setTelefono(cmd.telefono());
        if (cmd.indirizzo() != null) entity.setIndirizzo(cmd.indirizzo());
        if (cmd.citta() != null) entity.setCitta(cmd.citta());
        if (cmd.provincia() != null) entity.setProvincia(cmd.provincia());
        if (cmd.cap() != null) entity.setCap(cmd.cap());

        var saved = personRepository.save(entity);
        log.info("[UPDATE_PERSON] Updated personId={}", saved.getId());

        eventPublisher.publish(
                KafkaTopics.PERSON_UPDATED,
                new PersonUpdatedEvent(
                        UUID.randomUUID(), saved.getId(), saved.getNome(),
                        saved.getCognome(), saved.getEmail(), Instant.now()),
                null, null);

        return new UpdatePersonResult.Updated(saved.getId());
    }
}
