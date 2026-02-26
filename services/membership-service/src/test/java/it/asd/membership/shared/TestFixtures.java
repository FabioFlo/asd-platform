package it.asd.membership.shared;

import it.asd.membership.features.addtogroup.AddToGroupCommand;
import it.asd.membership.features.creategroup.CreateGroupCommand;
import it.asd.membership.features.enrollmember.EnrollMemberCommand;
import it.asd.membership.shared.entity.GroupEntity;
import it.asd.membership.shared.entity.GroupStatus;
import it.asd.membership.shared.entity.MembershipEntity;
import it.asd.membership.shared.entity.MembershipStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Centralized test data for membership-service.
 * All test classes import from here — never build entities inline.
 */
public final class TestFixtures {

    private TestFixtures() {}

    // ── Commands ──────────────────────────────────────────────────────────────

    public static EnrollMemberCommand validEnrollMemberCommand() {
        return new EnrollMemberCommand(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.of(2024, 9, 1),
                "Test membership note"
        );
    }

    public static CreateGroupCommand validCreateGroupCommand(UUID asdId) {
        return new CreateGroupCommand(
                asdId,
                UUID.randomUUID(),
                "Nuoto Agonistico",
                "Nuoto",
                "Agonistico",
                "Test group note"
        );
    }

    public static AddToGroupCommand validAddToGroupCommand(UUID membershipPersonId, UUID groupId) {
        return new AddToGroupCommand(
                membershipPersonId,
                groupId,
                UUID.randomUUID(),
                "Atleta",
                LocalDate.of(2024, 9, 15)
        );
    }

    // ── Entities (for repository stubs in unit tests) ─────────────────────────

    public static MembershipEntity savedMembership(UUID id) {
        return MembershipEntity.builder()
                .id(id)
                .personId(UUID.randomUUID())
                .asdId(UUID.randomUUID())
                .seasonId(UUID.randomUUID())
                .numeroTessera("ABCD-2024-12345678")
                .dataIscrizione(LocalDate.of(2024, 9, 1))
                .stato(MembershipStatus.ACTIVE)
                .note("Test note")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static MembershipEntity savedMembership() {
        return savedMembership(UUID.randomUUID());
    }

    public static GroupEntity savedGroup(UUID id, UUID asdId) {
        return GroupEntity.builder()
                .id(id)
                .asdId(asdId)
                .seasonId(UUID.randomUUID())
                .nome("Nuoto Agonistico")
                .disciplina("Nuoto")
                .tipo("Agonistico")
                .stato(GroupStatus.ACTIVE)
                .note("Test group note")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static GroupEntity savedGroup() {
        return savedGroup(UUID.randomUUID(), UUID.randomUUID());
    }
}
