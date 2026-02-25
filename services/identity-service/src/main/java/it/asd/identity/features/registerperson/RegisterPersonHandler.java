package it.asd.identity.features.registerperson;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.events.identity.PersonCreatedEvent;
import it.asd.identity.shared.entity.PersonEntity;
import it.asd.identity.shared.entity.PersonStatus;
import it.asd.identity.shared.repository.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class RegisterPersonHandler {

    private static final Logger log = LoggerFactory.getLogger(RegisterPersonHandler.class);

    private final PersonRepository personRepository;
    private final EventPublisher eventPublisher;

    public RegisterPersonHandler(PersonRepository personRepository, EventPublisher eventPublisher) {
        this.personRepository = personRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RegisterPersonResult handle(RegisterPersonCommand cmd) {
        if (personRepository.findByCodiceFiscale(cmd.codiceFiscale()).isPresent()) {
            return new RegisterPersonResult.DuplicateCodiceFiscale(cmd.codiceFiscale());
        }

        if (cmd.email() != null && !cmd.email().isBlank()
                && personRepository.findByEmail(cmd.email()).isPresent()) {
            return new RegisterPersonResult.DuplicateEmail(cmd.email());
        }

        var entity = PersonEntity.builder()
                .codiceFiscale(cmd.codiceFiscale())
                .nome(cmd.nome())
                .cognome(cmd.cognome())
                .dataNascita(cmd.dataNascita())
                .luogoNascita(cmd.luogoNascita())
                .codiceProvinciaNascita(cmd.codiceProvinciaNascita())
                .email(cmd.email())
                .telefono(cmd.telefono())
                .indirizzo(cmd.indirizzo())
                .citta(cmd.citta())
                .provincia(cmd.provincia())
                .cap(cmd.cap())
                .stato(PersonStatus.ACTIVE)
                .build();

        var saved = personRepository.save(entity);
        log.info("[REGISTER_PERSON] Saved personId={} cf={}", saved.getId(), saved.getCodiceFiscale());

        eventPublisher.publish(
                KafkaTopics.PERSON_CREATED,
                new PersonCreatedEvent(
                        UUID.randomUUID(), saved.getId(), saved.getCodiceFiscale(),
                        saved.getNome(), saved.getCognome(), saved.getEmail(), Instant.now()),
                null, null);

        return new RegisterPersonResult.Registered(saved.getId());
    }
}
