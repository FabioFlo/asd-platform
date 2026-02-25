package it.asd.membership.features.addtogroup;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.events.membership.GroupEnrollmentAddedEvent;
import it.asd.membership.shared.entity.EnrollmentStatus;
import it.asd.membership.shared.entity.GroupEnrollmentEntity;
import it.asd.membership.shared.entity.GroupStatus;
import it.asd.membership.shared.entity.MembershipStatus;
import it.asd.membership.shared.repository.GroupEnrollmentRepository;
import it.asd.membership.shared.repository.GroupRepository;
import it.asd.membership.shared.repository.MembershipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class AddToGroupHandler {

    private static final Logger log = LoggerFactory.getLogger(AddToGroupHandler.class);

    private final GroupRepository groupRepository;
    private final MembershipRepository membershipRepository;
    private final GroupEnrollmentRepository enrollmentRepository;
    private final EventPublisher eventPublisher;

    public AddToGroupHandler(GroupRepository groupRepository,
                             MembershipRepository membershipRepository,
                             GroupEnrollmentRepository enrollmentRepository,
                             EventPublisher eventPublisher) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AddToGroupResult handle(AddToGroupCommand cmd) {
        // 1. Verify group exists and is ACTIVE
        var groupOpt = groupRepository.findById(cmd.groupId());
        if (groupOpt.isEmpty() || groupOpt.get().getStato() != GroupStatus.ACTIVE) {
            return new AddToGroupResult.GroupNotFound(cmd.groupId());
        }
        var group = groupOpt.get();

        // 2. Verify person has ACTIVE membership for this asd+season
        var memberships = membershipRepository.findByPersonIdAndAsdIdAndStato(
                cmd.personId(), group.getAsdId(), MembershipStatus.ACTIVE);
        var activeMembership = memberships.stream()
                .filter(m -> m.getSeasonId().equals(cmd.seasonId()))
                .findFirst();
        if (activeMembership.isEmpty()) {
            return new AddToGroupResult.NotAMember(
                    "Person " + cmd.personId() + " has no active membership for this season");
        }

        // 3. Check no existing ACTIVE enrollment
        var existingEnrollment = enrollmentRepository.findByPersonIdAndGroupIdAndSeasonId(
                cmd.personId(), cmd.groupId(), cmd.seasonId());
        if (existingEnrollment.isPresent() && existingEnrollment.get().getStato() == EnrollmentStatus.ACTIVE) {
            return new AddToGroupResult.AlreadyInGroup(existingEnrollment.get().getId());
        }

        // 4. Save enrollment
        var entity = GroupEnrollmentEntity.builder()
                .personId(cmd.personId())
                .groupId(cmd.groupId())
                .asdId(group.getAsdId())
                .seasonId(cmd.seasonId())
                .ruolo(cmd.ruolo())
                .dataIngresso(cmd.dataIngresso())
                .stato(EnrollmentStatus.ACTIVE)
                .build();

        var saved = enrollmentRepository.save(entity);
        log.info("[ADD_TO_GROUP] Saved enrollmentId={} personId={} groupId={}",
                saved.getId(), saved.getPersonId(), saved.getGroupId());

        // 5. Publish event
        eventPublisher.publish(
                KafkaTopics.GROUP_ENROLLMENT_ADDED,
                new GroupEnrollmentAddedEvent(
                        UUID.randomUUID(), saved.getId(), saved.getPersonId(), saved.getGroupId(),
                        saved.getAsdId(), saved.getSeasonId(), saved.getRuolo(),
                        saved.getDataIngresso(), Instant.now()),
                saved.getAsdId(), saved.getSeasonId());

        return new AddToGroupResult.Added(saved.getId());
    }
}
