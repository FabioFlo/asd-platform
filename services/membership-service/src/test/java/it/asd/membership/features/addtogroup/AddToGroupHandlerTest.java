package it.asd.membership.features.addtogroup;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.membership.shared.TestFixtures;
import it.asd.membership.shared.entity.EnrollmentStatus;
import it.asd.membership.shared.entity.GroupEnrollmentEntity;
import it.asd.membership.shared.entity.GroupStatus;
import it.asd.membership.shared.entity.MembershipStatus;
import it.asd.membership.shared.repository.GroupEnrollmentRepository;
import it.asd.membership.shared.repository.GroupRepository;
import it.asd.membership.shared.repository.MembershipRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddToGroupHandler")
@Tag("unit")
class AddToGroupHandlerTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private GroupEnrollmentRepository enrollmentRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private AddToGroupHandler handler;

    @Nested
    @DisplayName("when group is active, person has active membership, and is not already in the group")
    class WhenValid {

        @Test
        @DisplayName("returns Added, saves enrollment, and publishes group.enrollment.added event")
        void returnsAdded() {
            var asdId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var personId = UUID.randomUUID();
            var seasonId = UUID.randomUUID();

            var group = TestFixtures.savedGroup(groupId, asdId);

            var membership = TestFixtures.savedMembership(UUID.randomUUID());
            membership.setPersonId(personId);
            membership.setAsdId(asdId);
            membership.setSeasonId(seasonId);
            membership.setStato(MembershipStatus.ACTIVE);

            var cmd = new AddToGroupCommand(personId, groupId, seasonId, "Atleta", LocalDate.of(2024, 9, 15));

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(membershipRepository.findByPersonIdAndAsdIdAndStato(personId, asdId, MembershipStatus.ACTIVE))
                    .thenReturn(List.of(membership));
            when(enrollmentRepository.findByPersonIdAndGroupIdAndSeasonId(personId, groupId, seasonId))
                    .thenReturn(Optional.empty());
            when(enrollmentRepository.save(any())).thenAnswer(inv -> {
                GroupEnrollmentEntity e = inv.getArgument(0);
                e.setId(UUID.randomUUID());
                return e;
            });

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(AddToGroupResult.Added.class);
            var added = (AddToGroupResult.Added) result;
            assertThat(added.enrollmentId()).isNotNull();

            verify(enrollmentRepository).save(any());
            verify(eventPublisher).publish(eq(KafkaTopics.GROUP_ENROLLMENT_ADDED), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("when group does not exist or is not active")
    class WhenGroupNotFound {

        @Test
        @DisplayName("returns GroupNotFound without saving or publishing")
        void returnsGroupNotFound() {
            var groupId = UUID.randomUUID();
            var cmd = TestFixtures.validAddToGroupCommand(UUID.randomUUID(), groupId);

            when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(AddToGroupResult.GroupNotFound.class);
            var notFound = (AddToGroupResult.GroupNotFound) result;
            assertThat(notFound.groupId()).isEqualTo(groupId);

            verify(enrollmentRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("returns GroupNotFound when group exists but is not ACTIVE")
        void returnsGroupNotFoundWhenInactive() {
            var asdId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var cmd = TestFixtures.validAddToGroupCommand(UUID.randomUUID(), groupId);

            var inactiveGroup = TestFixtures.savedGroup(groupId, asdId);
            inactiveGroup.setStato(GroupStatus.ARCHIVED);

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(inactiveGroup));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(AddToGroupResult.GroupNotFound.class);

            verify(enrollmentRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when person has no active membership for this season")
    class WhenNotAMember {

        @Test
        @DisplayName("returns NotAMember without saving or publishing")
        void returnsNotAMember() {
            var asdId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var personId = UUID.randomUUID();
            var seasonId = UUID.randomUUID();

            var group = TestFixtures.savedGroup(groupId, asdId);
            var cmd = new AddToGroupCommand(personId, groupId, seasonId, "Atleta", LocalDate.of(2024, 9, 15));

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(membershipRepository.findByPersonIdAndAsdIdAndStato(personId, asdId, MembershipStatus.ACTIVE))
                    .thenReturn(List.of());

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(AddToGroupResult.NotAMember.class);

            verify(enrollmentRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }

    @Nested
    @DisplayName("when person is already actively enrolled in the group for this season")
    class WhenAlreadyInGroup {

        @Test
        @DisplayName("returns AlreadyInGroup without saving or publishing")
        void returnsAlreadyInGroup() {
            var asdId = UUID.randomUUID();
            var groupId = UUID.randomUUID();
            var personId = UUID.randomUUID();
            var seasonId = UUID.randomUUID();
            var existingEnrollmentId = UUID.randomUUID();

            var group = TestFixtures.savedGroup(groupId, asdId);

            var membership = TestFixtures.savedMembership(UUID.randomUUID());
            membership.setPersonId(personId);
            membership.setAsdId(asdId);
            membership.setSeasonId(seasonId);
            membership.setStato(MembershipStatus.ACTIVE);

            var existingEnrollment = GroupEnrollmentEntity.builder()
                    .id(existingEnrollmentId)
                    .personId(personId)
                    .groupId(groupId)
                    .asdId(asdId)
                    .seasonId(seasonId)
                    .ruolo("Atleta")
                    .dataIngresso(LocalDate.of(2024, 9, 1))
                    .stato(EnrollmentStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            var cmd = new AddToGroupCommand(personId, groupId, seasonId, "Atleta", LocalDate.of(2024, 9, 15));

            when(groupRepository.findById(groupId)).thenReturn(Optional.of(group));
            when(membershipRepository.findByPersonIdAndAsdIdAndStato(personId, asdId, MembershipStatus.ACTIVE))
                    .thenReturn(List.of(membership));
            when(enrollmentRepository.findByPersonIdAndGroupIdAndSeasonId(personId, groupId, seasonId))
                    .thenReturn(Optional.of(existingEnrollment));

            var result = handler.handle(cmd);

            assertThat(result).isInstanceOf(AddToGroupResult.AlreadyInGroup.class);
            var alreadyInGroup = (AddToGroupResult.AlreadyInGroup) result;
            assertThat(alreadyInGroup.existingId()).isEqualTo(existingEnrollmentId);

            verify(enrollmentRepository, never()).save(any());
            verifyNoInteractions(eventPublisher);
        }
    }
}
