package it.asd.identity.integration;

import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.identity.features.registerperson.PersonResponse;
import it.asd.identity.features.registerperson.RegisterPersonCommand;
import it.asd.identity.shared.repository.PersonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("RegisterPerson — integration")
@Tag("integration")
class RegisterPersonIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PersonRepository repository;

    @Autowired
    private PersonCreatedEventCapture eventCapture;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        eventCapture.clear();
    }

    @Test
    @DisplayName("creates person in DB and publishes person.created event")
    void createsPersonAndPublishesEvent() {
        var cmd = new RegisterPersonCommand(
                "RSSMRA80A01H501Z", "Mario", "Rossi",
                LocalDate.of(1980, 1, 1), "Roma", 'H',
                "mario.rossi@example.com", null, null, null, null, null);

        ResponseEntity<PersonResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/identity/persons",
                cmd,
                PersonResponse.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isNotNull();

        var saved = repository.findById(response.getBody().id());
        assertThat(saved).isPresent();
        assertThat(saved.get().getNome()).isEqualTo("Mario");
        assertThat(saved.get().getCodiceFiscale()).isEqualTo("RSSMRA80A01H501Z");

        var personId = response.getBody().id().toString();
        await().atMost(10, SECONDS).untilAsserted(() ->
                assertThat(eventCapture.envelopes())
                        .anyMatch(e -> e.payload() != null && e.payload().aggregateId().equals(personId)));
    }

    @Test
    @DisplayName("returns 422 when codice fiscale already exists")
    void returns422OnDuplicateCf() {
        var cmd = new RegisterPersonCommand(
                "RSSMRA80A01H501Z", "Mario", "Rossi",
                null, null, 'X',
                null, null, null, null, null, null);

        restTemplate.postForEntity("http://localhost:" + port + "/identity/persons", cmd, Object.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/identity/persons",
                cmd,
                String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(422);
    }

    /**
     * Captures person.created Kafka envelopes for integration test assertions.
     */
    @Component
    static class PersonCreatedEventCapture {

        private final List<EventEnvelope> received = new CopyOnWriteArrayList<>();

        @KafkaListener(topics = KafkaTopics.PERSON_CREATED, groupId = "test-identity-capture")
        void onEvent(EventEnvelope envelope) {
            received.add(envelope);
        }

        List<EventEnvelope> envelopes() {
            return received;
        }

        void clear() {
            received.clear();
        }
    }
}
