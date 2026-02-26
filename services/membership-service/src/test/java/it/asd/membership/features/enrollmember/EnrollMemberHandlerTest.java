package it.asd.membership.features.enrollmember;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.membership.shared.TestFixtures;
import it.asd.membership.shared.entity.MembershipEntity;
import it.asd.membership.shared.repository.MembershipRepository;
import it.asd.membership.shared.repository.PersonCacheRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollMemberHandler")
@Tag("unit")
class EnrollMemberHandlerTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private PersonCacheRepository personCacheRepository;

    @Mock
    private IdentityClient identityClient;

    @Mock
    private RegistryClient registryClient;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private EnrollMemberHandler handler;

    @Nested
    @DisplayName("when person and season exist and no duplicate membership")
    class WhenValid {

        @Test
        @DisplayName("returns Enrolled, saves entity, and publishes membership.activated event")
        void returnsEnrolled() {
            var cmd = TestFixtures.validEnrollMemberCommand();

            when(membershipRepository.findByPersonIdAndAsdIdAndSeasonId(
                    cmd.personId(), cmd.asdId(), cmd.seasonId()))
                    .thenReturn(Optional.empty());

            when(identityClient.getPerson(cmd.personId()))
                    .thenReturn(new IdentityClient.PersonApiResponse(
                            cmd.personId(), "RSSMRA80A01H501Z", "Mario", "Rossi",
                            null, "mario.rossi@example.com", "ACTIVE"));

            when(registryClient.getCurrentSeason(cmd.asdId()))
                    .thenReturn(new RegistryClient.SeasonApiResponse(
                            cmd.seasonId(), cmd.asdId(), "2024-2025",
                            null, null));

            when(membershipRepository.save(any())).thenAnswer(inv -> {
                MembershipEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            when(personCacheRepository.findByPersonId(cmd.personId()))
                    .thenReturn(Optional.empty());

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(EnrollMemberResult.Enrolled.class);
            var enrolled = (EnrollMemberResult.Enrolled) result;
            assertThat(enrolled.membershipId()).isNotNull();
            assertThat(enrolled.numeroTessera()).isNotBlank();

            verify(membershipRepository).save(any());
            verify(eventPublisher).publish(eq(KafkaTopics.MEMBERSHIP_ACTIVATED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when person is already enrolled for this asd and season")
    class WhenAlreadyEnrolled {

        @Test
        @DisplayName("returns AlreadyEnrolled without saving or publishing")
        void returnsAlreadyEnrolled() {
            var cmd = TestFixtures.validEnrollMemberCommand();
            var existingId = UUID.randomUUID();
            var existing = TestFixtures.savedMembership(existingId);

            when(membershipRepository.findByPersonIdAndAsdIdAndSeasonId(
                    cmd.personId(), cmd.asdId(), cmd.seasonId()))
                    .thenReturn(Optional.of(existing));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(EnrollMemberResult.AlreadyEnrolled.class);
            var alreadyEnrolled = (EnrollMemberResult.AlreadyEnrolled) result;
            assertThat(alreadyEnrolled.existingId()).isEqualTo(existingId);

            verify(membershipRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when identity-service cannot find the person")
    class WhenPersonNotFound {

        @Test
        @DisplayName("returns PersonNotFound without saving or publishing")
        void returnsPersonNotFound() {
            var cmd = TestFixtures.validEnrollMemberCommand();

            when(membershipRepository.findByPersonIdAndAsdIdAndSeasonId(
                    cmd.personId(), cmd.asdId(), cmd.seasonId()))
                    .thenReturn(Optional.empty());

            when(identityClient.getPerson(cmd.personId()))
                    .thenThrow(new IdentityClient.IdentityCallException("Person not found"));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(EnrollMemberResult.PersonNotFound.class);
            var notFound = (EnrollMemberResult.PersonNotFound) result;
            assertThat(notFound.personId()).isEqualTo(cmd.personId());

            verify(membershipRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when registry-service cannot find an active season for the ASD")
    class WhenSeasonNotFound {

        @Test
        @DisplayName("returns SeasonNotFound without saving or publishing")
        void returnsSeasonNotFound() {
            var cmd = TestFixtures.validEnrollMemberCommand();

            when(membershipRepository.findByPersonIdAndAsdIdAndSeasonId(
                    cmd.personId(), cmd.asdId(), cmd.seasonId()))
                    .thenReturn(Optional.empty());

            when(identityClient.getPerson(cmd.personId()))
                    .thenReturn(new IdentityClient.PersonApiResponse(
                            cmd.personId(), "RSSMRA80A01H501Z", "Mario", "Rossi",
                            null, "mario.rossi@example.com", "ACTIVE"));

            when(registryClient.getCurrentSeason(cmd.asdId()))
                    .thenThrow(new RegistryClient.RegistryCallException("No active season"));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(EnrollMemberResult.SeasonNotFound.class);
            var seasonNotFound = (EnrollMemberResult.SeasonNotFound) result;
            assertThat(seasonNotFound.asdId()).isEqualTo(cmd.asdId());

            verify(membershipRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }
}
