package it.asd.competition.shared;

import it.asd.competition.features.registerparticipant.RegisterParticipantCommand;
import it.asd.competition.features.recordresult.RecordResultCommand;
import it.asd.competition.shared.entity.EligibilityCacheEntity;
import it.asd.competition.shared.entity.EventParticipationEntity;
import it.asd.competition.shared.entity.ParticipationStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Centralized test data for competition-service.
 * All test classes import from here — never build entities inline.
 *
 * Keep builders minimal: use sensible defaults, let callers override
 * only what matters for their specific test case.
 */
public final class TestFixtures {

    private TestFixtures() {}

    // ── Well-known IDs ────────────────────────────────────────────────────────

    public static final UUID EVENT_ID         = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    public static final UUID ASD_ID           = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    public static final UUID SEASON_ID        = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    public static final UUID PERSON_ID        = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    public static final UUID PARTICIPATION_ID = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");

    // ── RegisterParticipant commands ──────────────────────────────────────────

    /** Valid individual (person-based) registration command. */
    public static RegisterParticipantCommand validRegisterParticipantCommand() {
        return new RegisterParticipantCommand(
                EVENT_ID,
                ASD_ID,
                SEASON_ID,
                PERSON_ID,
                null,           // no groupId — individual event
                "SENIOR",
                true            // agonistic
        );
    }

    /** Team registration command (groupId set, personId null). */
    public static RegisterParticipantCommand teamRegisterParticipantCommand() {
        return new RegisterParticipantCommand(
                EVENT_ID,
                ASD_ID,
                SEASON_ID,
                null,                   // no personId — team event
                UUID.randomUUID(),      // groupId
                "UNDER21",
                false
        );
    }

    // ── RecordResult commands ─────────────────────────────────────────────────

    public static RecordResultCommand validRecordResultCommand() {
        return new RecordResultCommand(
                PARTICIPATION_ID,
                1,
                BigDecimal.valueOf(9.85),
                Map.of("notes", "personal record")
        );
    }

    public static RecordResultCommand recordResultCommandWithoutScore() {
        return new RecordResultCommand(
                PARTICIPATION_ID,
                3,
                null,
                null
        );
    }

    // ── Entities (for repository stubs in unit tests) ─────────────────────────

    public static EventParticipationEntity savedParticipation(UUID id) {
        return EventParticipationEntity.builder()
                .id(id)
                .eventId(EVENT_ID)
                .personId(PERSON_ID)
                .groupId(null)
                .asdId(ASD_ID)
                .seasonId(SEASON_ID)
                .categoria("SENIOR")
                .stato(ParticipationStatus.REGISTERED)
                .build();
    }

    public static EventParticipationEntity savedParticipation() {
        return savedParticipation(UUID.randomUUID());
    }

    public static EventParticipationEntity participationWithResult(UUID id) {
        return EventParticipationEntity.builder()
                .id(id)
                .eventId(EVENT_ID)
                .personId(PERSON_ID)
                .asdId(ASD_ID)
                .seasonId(SEASON_ID)
                .categoria("SENIOR")
                .stato(ParticipationStatus.PARTICIPATED)
                .posizione(1)
                .punteggio(BigDecimal.valueOf(9.85))
                .resultData(Map.of("notes", "personal record"))
                .build();
    }

    public static EligibilityCacheEntity eligibleCacheEntry() {
        return EligibilityCacheEntity.builder()
                .id(UUID.randomUUID())
                .personId(PERSON_ID)
                .asdId(ASD_ID)
                .eligible(true)
                .source("sync_check")
                .build();
    }

    public static EligibilityCacheEntity ineligibleCacheEntry() {
        return EligibilityCacheEntity.builder()
                .id(UUID.randomUUID())
                .personId(PERSON_ID)
                .asdId(ASD_ID)
                .eligible(false)
                .blockingDocuments(List.of("CERTIFICATO_MEDICO_AGONISTICO [EXPIRED on 2024-01-01]"))
                .source("document.expired")
                .build();
    }
}
