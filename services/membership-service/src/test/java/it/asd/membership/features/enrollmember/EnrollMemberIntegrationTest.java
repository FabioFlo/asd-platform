package it.asd.membership.features.enrollmember;

import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.membership.integration.BaseIntegrationTest;
import it.asd.membership.shared.repository.MembershipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("EnrollMember — integration")
@Tag("integration")
class EnrollMemberIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private MembershipActivatedEventCapture eventCapture;

    @MockBean
    private IdentityClient identityClient;

    @MockBean
    private RegistryClient registryClient;

    private static final UUID PERSON_ID  = UUID.randomUUID();
    private static final UUID ASD_ID     = UUID.randomUUID();
    private static final UUID SEASON_ID  = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        membershipRepository.deleteAll();
        eventCapture.clear();

        when(identityClient.getPerson(any()))
                .thenReturn(new IdentityClient.PersonApiResponse(
                        PERSON_ID, "RSSMRA80A01H501Z", "Mario", "Rossi",
                        LocalDate.of(1980, 1, 1), "mario.rossi@example.com", "ACTIVE"));

        when(registryClient.getCurrentSeason(any()))
                .thenReturn(new RegistryClient.SeasonApiResponse(
                        SEASON_ID, ASD_ID, "2024-2025",
                        LocalDate.of(2024, 9, 1), LocalDate.of(2025, 8, 31)));
    }

    @Test
    @DisplayName("enrolls member → 201, persists to DB, publishes membership.activated Kafka event")
    void enrollsMemberSuccessfully() {
        var cmd = new EnrollMemberCommand(PERSON_ID, ASD_ID, SEASON_ID,
                LocalDate.of(2024, 9, 1), "Integration test enrollment");

        ResponseEntity<MembershipResponse> response = restTemplate.postForEntity(
                "http://localhost:" + port + "/membership/members",
                cmd,
                MembershipResponse.class);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().membershipId()).isNotNull();
        assertThat(response.getBody().numeroTessera()).isNotBlank();

        var membershipId = response.getBody().membershipId();
        var saved = membershipRepository.findById(membershipId);
        assertThat(saved).isPresent();
        assertThat(saved.get().getPersonId()).isEqualTo(PERSON_ID);
        assertThat(saved.get().getAsdId()).isEqualTo(ASD_ID);
        assertThat(saved.get().getSeasonId()).isEqualTo(SEASON_ID);

        var membershipIdStr = membershipId.toString();
        await().atMost(10, SECONDS).untilAsserted(() ->
                assertThat(eventCapture.envelopes())
                        .anyMatch(e -> e.payload() != null
                                && e.payload().aggregateId().equals(membershipIdStr)));
    }

    @Test
    @DisplayName("re-enrolling same person → 409 ALREADY_ENROLLED")
    void returnsConflictOnReEnrollment() {
        var cmd = new EnrollMemberCommand(PERSON_ID, ASD_ID, SEASON_ID,
                LocalDate.of(2024, 9, 1), "First enrollment");

        restTemplate.postForEntity(
                "http://localhost:" + port + "/membership/members",
                cmd,
                MembershipResponse.class);

        ResponseEntity<String> secondResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/membership/members",
                cmd,
                String.class);

        assertThat(secondResponse.getStatusCode().value()).isEqualTo(409);
    }

    /** Captures membership.activated Kafka envelopes for integration test assertions. */
    @Component
    static class MembershipActivatedEventCapture {

        private final List<EventEnvelope> received = new CopyOnWriteArrayList<>();

        @KafkaListener(topics = KafkaTopics.MEMBERSHIP_ACTIVATED, groupId = "test-membership-capture")
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
