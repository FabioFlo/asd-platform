package it.asd.competition.features.registerparticipant;

import it.asd.competition.integration.BaseIntegrationTest;
import it.asd.competition.shared.TestFixtures;
import it.asd.competition.shared.entity.ParticipationStatus;
import it.asd.competition.shared.repository.EligibilityCacheRepository;
import it.asd.competition.shared.repository.EventParticipationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;

/**
 * Integration test for the RegisterParticipant vertical slice.
 * <p>
 * ComplianceClient is package-private, so this test lives in the same package.
 * It still extends the public BaseIntegrationTest from the integration package.
 * <p>
 * ComplianceClient is a Spring @Component that makes HTTP calls to an external
 * service. In integration tests the compliance-service is not running, so we
 *
 * @MockBean it to control eligibility answers.
 */
@DisplayName("RegisterParticipant — integration")
@Tag("integration")
public class RegisterParticipantIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventParticipationRepository participationRepository;

    @Autowired
    private EligibilityCacheRepository cacheRepository;

    /**
     * Mock the HTTP client so tests do not require a live compliance-service.
     * Default behaviour (before any stubbing): returns eligible=true.
     */
    @MockBean
    private ComplianceClient complianceClient;

    @BeforeEach
    void setUp() {
        participationRepository.deleteAll();
        cacheRepository.deleteAll();
        // Default: compliance says eligible
        when(complianceClient.checkEligibility(any(), any(), anyBoolean()))
                .thenReturn(new ComplianceClient.EligibilityApiResponse(true, List.of(), List.of()));
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("returns 201 and persists participation when person is eligible")
    void createsParticipationWhenEligible() {
        var response = register(validCommand());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();

        @SuppressWarnings("unchecked")
        var body = (Map<String, Object>) response.getBody();
        assertThat(body).containsKey("participationId");
        assertThat(body.get("eventId")).isEqualTo(TestFixtures.EVENT_ID.toString());

        var all = participationRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getPersonId()).isEqualTo(TestFixtures.PERSON_ID);
        assertThat(all.get(0).getStato()).isEqualTo(ParticipationStatus.REGISTERED);
        assertThat(all.get(0).getCategoria()).isEqualTo("SENIOR");
    }

    // ── AlreadyRegistered → 409 ───────────────────────────────────────────────

    @Test
    @DisplayName("returns 409 when person is already registered for the same event")
    void returns409OnDuplicateRegistration() {
        var first = register(validCommand());
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        var second = register(validCommand());
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(second.getBody().toString()).contains("ALREADY_REGISTERED");
    }

    // ── Ineligible → 422 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("returns 422 when compliance says person is ineligible")
    void returns422WhenIneligible() {
        when(complianceClient.checkEligibility(any(), any(), anyBoolean()))
                .thenReturn(new ComplianceClient.EligibilityApiResponse(
                        false,
                        List.of("CERTIFICATO_MEDICO_AGONISTICO"),
                        List.of()));

        var response = register(validCommand());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody().toString()).contains("PERSON_INELIGIBLE");
        assertThat(participationRepository.findAll()).isEmpty();
    }

    // ── ComplianceUnavailable / fail-closed → 503 ─────────────────────────────

    @Test
    @DisplayName("returns 503 with Retry-After when compliance is unreachable (fail-closed)")
    void returns503WhenComplianceUnreachable() {
        when(complianceClient.checkEligibility(any(), any(), anyBoolean()))
                .thenThrow(new ComplianceClient.ComplianceCallException("Connection refused"));

        var response = register(validCommand());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getHeaders().containsKey("Retry-After")).isTrue();
        assertThat(response.getBody().toString()).contains("SERVICE_UNAVAILABLE");
        assertThat(participationRepository.findAll()).isEmpty();
    }

    // ── Eligible from cache (warm path) ───────────────────────────────────────

    @Test
    @DisplayName("uses cache when warm and skips compliance call")
    void usesEligibilityCacheOnSecondAttempt() {
        // First call populates the cache via the mocked compliance client
        register(validCommand());

        // Cache is now warm (eligible=true). Reset mock to fail for a second DIFFERENT event.
        when(complianceClient.checkEligibility(any(), any(), anyBoolean()))
                .thenThrow(new ComplianceClient.ComplianceCallException("Should not be called"));

        var cmd = new RegisterParticipantCommand(
                java.util.UUID.randomUUID(),   // different event
                TestFixtures.ASD_ID,
                TestFixtures.SEASON_ID,
                TestFixtures.PERSON_ID,
                null,
                "SENIOR",
                true);

        var response = register(cmd);

        // Cache says eligible → registration must succeed even though compliance is "down"
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(participationRepository.findAll()).hasSize(2);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private RegisterParticipantCommand validCommand() {
        return TestFixtures.validRegisterParticipantCommand();
    }

    private ResponseEntity<Object> register(RegisterParticipantCommand cmd) {
        return restTemplate.postForEntity(
                "http://localhost:" + port
                        + "/competition/events/" + cmd.eventId() + "/participants",
                cmd,
                Object.class);
    }
}
