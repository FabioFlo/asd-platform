package it.asd.scheduling.integration;

import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.scheduling.shared.entity.SessionType;
import it.asd.scheduling.shared.repository.RoomRepository;
import it.asd.scheduling.shared.repository.SessionRepository;
import it.asd.scheduling.shared.repository.VenueRepository;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("ScheduleSession — integration")
@Tag("integration")
class ScheduleSessionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionScheduledEventCapture eventCapture;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        roomRepository.deleteAll();
        venueRepository.deleteAll();
        eventCapture.clear();
    }

    @Test
    @DisplayName("creates session in DB and publishes session.scheduled event")
    void createsSessionAndPublishesEvent() {
        UUID asdId = UUID.randomUUID();

        // Create a venue
        var createVenueBody = Map.of(
                "asdId", asdId.toString(),
                "nome", "Piscina Comunale",
                "indirizzo", "Via Piscina 1",
                "citta", "Roma",
                "provincia", "RM");
        var venueResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/scheduling/venues",
                createVenueBody, Map.class);
        assertThat(venueResponse.getStatusCode().value()).isEqualTo(201);
        UUID venueId = UUID.fromString(venueResponse.getBody().get("venueId").toString());

        // Schedule a session
        var sessionBody = Map.of(
                "asdId", asdId.toString(),
                "venueId", venueId.toString(),
                "titolo", "Allenamento Mattutino",
                "data", "2026-06-15",
                "oraInizio", "09:00:00",
                "oraFine", "11:00:00",
                "tipo", SessionType.TRAINING.name());
        ResponseEntity<Map> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/scheduling/sessions",
                sessionBody, Map.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        UUID sessionId = UUID.fromString(response.getBody().get("sessionId").toString());

        var saved = sessionRepository.findById(sessionId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getTitolo()).isEqualTo("Allenamento Mattutino");
        assertThat(saved.get().getVenueId()).isEqualTo(venueId);
        assertThat(saved.get().getAsdId()).isEqualTo(asdId);

        await().atMost(10, SECONDS).untilAsserted(() ->
                assertThat(eventCapture.envelopes())
                        .anyMatch(e -> asdId.equals(e.asdId())));
    }

    @Test
    @DisplayName("returns 422 when time range is invalid (oraInizio after oraFine)")
    void returns422OnInvalidTimeRange() {
        UUID asdId = UUID.randomUUID();

        var createVenueBody = Map.of(
                "asdId", asdId.toString(),
                "nome", "Palasport",
                "indirizzo", "Via Sport 10",
                "citta", "Milano",
                "provincia", "MI");
        var venueResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/scheduling/venues",
                createVenueBody, Map.class);
        UUID venueId = UUID.fromString(venueResponse.getBody().get("venueId").toString());

        var sessionBody = Map.of(
                "asdId", asdId.toString(),
                "venueId", venueId.toString(),
                "titolo", "Sessione Invalida",
                "data", "2026-06-15",
                "oraInizio", "12:00:00",   // AFTER oraFine
                "oraFine", "09:00:00",
                "tipo", SessionType.TRAINING.name());
        var response = restTemplate.postForEntity(
                "http://localhost:" + port + "/scheduling/sessions",
                sessionBody, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(422);
    }

    /** Captures session.scheduled Kafka envelopes for integration test assertions. */
    @Component
    static class SessionScheduledEventCapture {

        private final List<EventEnvelope> received = new CopyOnWriteArrayList<>();

        @KafkaListener(topics = KafkaTopics.SESSION_SCHEDULED, groupId = "test-scheduling-capture")
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
