package it.asd.registry.integration;

import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.registry.features.activateseason.ActivateSeasonCommand;
import it.asd.registry.features.activateseason.SeasonResponse;
import it.asd.registry.features.createasd.CreateAsdCommand;
import it.asd.registry.shared.repository.AsdRepository;
import it.asd.registry.shared.repository.SeasonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("ActivateSeason — integration")
@Tag("integration")
class ActivateSeasonIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AsdRepository asdRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private SeasonActivatedEventCapture eventCapture;

    @BeforeEach
    void setUp() {
        seasonRepository.deleteAll();
        asdRepository.deleteAll();
        eventCapture.clear();
    }

    @Test
    @DisplayName("creates ASD and season in DB, publishes season.activated event")
    void createsSeasonAndPublishesEvent() {
        // First create an ASD
        var createAsdCmd = new CreateAsdCommand(
                "12345678901", "ASD Test Nuoto",
                null, null, "Nuoto", "Roma", "RM", null, null);
        var asdResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/registry/asd",
                createAsdCmd, java.util.Map.class);
        assertThat(asdResponse.getStatusCode().value()).isEqualTo(201);

        UUID asdId = UUID.fromString(asdResponse.getBody().get("id").toString());

        // Activate a season for the ASD
        var cmd = new ActivateSeasonCommand(
                asdId, "2025-2026",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2026, 6, 30));
        ResponseEntity<SeasonResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/registry/asd/" + asdId + "/seasons",
                cmd, SeasonResponse.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().seasonId()).isNotNull();

        var saved = seasonRepository.findById(response.getBody().seasonId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getCodice()).isEqualTo("2025-2026");
        assertThat(saved.get().getAsdId()).isEqualTo(asdId);

        await().atMost(10, SECONDS).untilAsserted(() ->
                assertThat(eventCapture.envelopes())
                        .anyMatch(e -> asdId.equals(e.asdId())));
    }

    @Test
    @DisplayName("returns 409 when ASD already has an active season")
    void returns409WhenAlreadyActiveSeason() {
        var createAsdCmd = new CreateAsdCommand(
                "99999999901", "ASD Altro",
                null, null, null, null, null, null, null);
        var asdResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/registry/asd",
                createAsdCmd, java.util.Map.class);
        UUID asdId = UUID.fromString(asdResponse.getBody().get("id").toString());

        var cmd = new ActivateSeasonCommand(
                asdId, "2025-2026",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2026, 6, 30));

        restTemplate.postForEntity("http://localhost:" + port + "/registry/asd/" + asdId + "/seasons",
                cmd, Object.class);

        var response = restTemplate.postForEntity(
                "http://localhost:" + port + "/registry/asd/" + asdId + "/seasons",
                cmd, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(409);
    }

    // ── Kafka capture ─────────────────────────────────────────────────────────

    @TestConfiguration
    static class KafkaTestConfig {
        @Bean
        SeasonActivatedEventCapture seasonActivatedEventCapture() {
            return new SeasonActivatedEventCapture();
        }
    }

    static class SeasonActivatedEventCapture {

        private final List<EventEnvelope> received = new CopyOnWriteArrayList<>();

        @KafkaListener(topics = KafkaTopics.SEASON_ACTIVATED, groupId = "test-registry-capture")
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
