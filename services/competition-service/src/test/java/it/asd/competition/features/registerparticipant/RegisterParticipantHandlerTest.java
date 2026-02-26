package it.asd.competition.features.registerparticipant;

import it.asd.common.kafka.EventPublisher;
import it.asd.competition.features.eligibilitycache.EligibilityCacheService;
import it.asd.competition.shared.TestFixtures;
import it.asd.competition.shared.repository.EventParticipationRepository;
import it.asd.events.KafkaTopics;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterParticipantHandler")
@Tag("unit")
class RegisterParticipantHandlerTest {

    @Mock
    private EventParticipationRepository participationRepo;

    @Mock
    private EligibilityCacheService eligibilityCache;

    @Mock
    private ComplianceClient complianceClient;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private RegisterParticipantHandler handler;

    // ── Registered ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("when person is eligible (cache hit)")
    class WhenEligibleFromCache {

        @Test
        @DisplayName("returns Registered, saves participation, and publishes event")
        void returnsRegistered() {
            var cmd = TestFixtures.validRegisterParticipantCommand();
            var savedEntity = TestFixtures.savedParticipation(UUID.randomUUID());

            when(participationRepo.findByPersonIdAndAsdId(cmd.personId(), cmd.asdId()))
                    .thenReturn(List.of());
            when(eligibilityCache.get(cmd.personId(), cmd.asdId()))
                    .thenReturn(Optional.of(new EligibilityCacheService.CachedEligibility(true, List.of())));
            when(participationRepo.save(any())).thenReturn(savedEntity);

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RegisterParticipantResult.Registered.class);
            verify(participationRepo).save(any());
            verify(eventPublisher).publish(
                    eq(KafkaTopics.PARTICIPANT_REGISTERED), any(), any(), any());
            verifyNoInteractions(complianceClient);
        }
    }

    @Nested
    @DisplayName("when cache is absent and compliance says eligible (cold path)")
    class WhenEligibleFromCompliance {

        @Test
        @DisplayName("returns Registered, updates cache, saves, and publishes event")
        void returnsRegistered() {
            var cmd = TestFixtures.validRegisterParticipantCommand();
            var savedEntity = TestFixtures.savedParticipation(UUID.randomUUID());

            when(participationRepo.findByPersonIdAndAsdId(cmd.personId(), cmd.asdId()))
                    .thenReturn(List.of());
            when(eligibilityCache.get(cmd.personId(), cmd.asdId()))
                    .thenReturn(Optional.empty());
            when(complianceClient.checkEligibility(cmd.personId(), cmd.asdId(), cmd.agonistic()))
                    .thenReturn(new ComplianceClient.EligibilityApiResponse(true, List.of(), List.of()));
            when(participationRepo.save(any())).thenReturn(savedEntity);

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RegisterParticipantResult.Registered.class);
            verify(eligibilityCache).updateFromSyncCheck(
                    eq(cmd.personId()), eq(cmd.asdId()), eq(true), any());
            verify(participationRepo).save(any());
            verify(eventPublisher).publish(
                    eq(KafkaTopics.PARTICIPANT_REGISTERED), any(), any(), any());
        }
    }

    // ── AlreadyRegistered ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("when person is already registered for this event")
    class WhenAlreadyRegistered {

        @Test
        @DisplayName("returns AlreadyRegistered without saving or publishing")
        void returnsAlreadyRegistered() {
            var cmd = TestFixtures.validRegisterParticipantCommand();
            var existing = TestFixtures.savedParticipation(UUID.randomUUID());
            // existing participation matches the same eventId
            existing.setEventId(cmd.eventId());

            when(participationRepo.findByPersonIdAndAsdId(cmd.personId(), cmd.asdId()))
                    .thenReturn(List.of(existing));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RegisterParticipantResult.AlreadyRegistered.class);
            verify(participationRepo, never()).save(any());
            verifyNoInteractions(eligibilityCache);
            verifyNoInteractions(complianceClient);
            verifyNoInteractions(eventPublisher);
        }
    }

    // ── Ineligible (cache hit) ────────────────────────────────────────────────

    @Nested
    @DisplayName("when cache says person is ineligible")
    class WhenIneligibleFromCache {

        @Test
        @DisplayName("returns Ineligible without saving or publishing")
        void returnsIneligible() {
            var cmd = TestFixtures.validRegisterParticipantCommand();
            var blocking = List.of("CERTIFICATO_MEDICO_AGONISTICO [EXPIRED on 2024-01-01]");

            when(participationRepo.findByPersonIdAndAsdId(cmd.personId(), cmd.asdId()))
                    .thenReturn(List.of());
            when(eligibilityCache.get(cmd.personId(), cmd.asdId()))
                    .thenReturn(Optional.of(new EligibilityCacheService.CachedEligibility(false, blocking)));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RegisterParticipantResult.Ineligible.class);
            var ineligible = (RegisterParticipantResult.Ineligible) result;
            assertThat(ineligible.blockingDocuments()).containsExactlyElementsOf(blocking);
            verify(participationRepo, never()).save(any());
            verifyNoInteractions(complianceClient);
            verifyNoInteractions(eventPublisher);
        }
    }

    // ── Ineligible (compliance cold path) ────────────────────────────────────

    @Nested
    @DisplayName("when cache is absent and compliance says ineligible")
    class WhenIneligibleFromCompliance {

        @Test
        @DisplayName("returns Ineligible, updates cache, without saving or publishing")
        void returnsIneligible() {
            var cmd = TestFixtures.validRegisterParticipantCommand();
            var blocking = List.of("CERTIFICATO_MEDICO_AGONISTICO");

            when(participationRepo.findByPersonIdAndAsdId(cmd.personId(), cmd.asdId()))
                    .thenReturn(List.of());
            when(eligibilityCache.get(cmd.personId(), cmd.asdId()))
                    .thenReturn(Optional.empty());
            when(complianceClient.checkEligibility(cmd.personId(), cmd.asdId(), cmd.agonistic()))
                    .thenReturn(new ComplianceClient.EligibilityApiResponse(false, blocking, List.of()));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RegisterParticipantResult.Ineligible.class);
            verify(eligibilityCache).updateFromSyncCheck(
                    eq(cmd.personId()), eq(cmd.asdId()), eq(false), eq(blocking));
            verify(participationRepo, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    // ── ComplianceUnavailable (fail-closed) ───────────────────────────────────

    @Nested
    @DisplayName("when cache is absent and compliance is unreachable (fail-closed)")
    class WhenComplianceUnavailable {

        @Test
        @DisplayName("returns ComplianceUnavailable without saving or publishing")
        void returnsComplianceUnavailable() {
            var cmd = TestFixtures.validRegisterParticipantCommand();

            when(participationRepo.findByPersonIdAndAsdId(cmd.personId(), cmd.asdId()))
                    .thenReturn(List.of());
            when(eligibilityCache.get(cmd.personId(), cmd.asdId()))
                    .thenReturn(Optional.empty());
            when(complianceClient.checkEligibility(cmd.personId(), cmd.asdId(), cmd.agonistic()))
                    .thenThrow(new ComplianceClient.ComplianceCallException("Connection refused"));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RegisterParticipantResult.ComplianceUnavailable.class);
            verify(participationRepo, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    // ── Team registration (no eligibility check) ──────────────────────────────

    @Nested
    @DisplayName("when registering a team (personId is null)")
    class WhenTeamRegistration {

        @Test
        @DisplayName("skips duplicate check and eligibility check, returns Registered")
        void registersTeamDirectly() {
            var cmd = TestFixtures.teamRegisterParticipantCommand();
            var savedEntity = TestFixtures.savedParticipation(UUID.randomUUID());
            savedEntity.setPersonId(null);
            savedEntity.setGroupId(cmd.groupId());

            when(participationRepo.save(any())).thenReturn(savedEntity);

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(RegisterParticipantResult.Registered.class);
            verify(participationRepo, never()).findByPersonIdAndAsdId(any(), any());
            verifyNoInteractions(eligibilityCache);
            verifyNoInteractions(complianceClient);
            verify(eventPublisher).publish(
                    eq(KafkaTopics.PARTICIPANT_REGISTERED), any(), any(), any());
        }
    }
}
