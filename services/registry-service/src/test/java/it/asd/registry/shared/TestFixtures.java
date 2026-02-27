package it.asd.registry.shared;

import it.asd.registry.features.activateseason.ActivateSeasonCommand;
import it.asd.registry.features.createasd.CreateAsdCommand;
import it.asd.registry.shared.entity.AsdEntity;
import it.asd.registry.shared.entity.AsdStatus;
import it.asd.registry.shared.entity.SeasonEntity;
import it.asd.registry.shared.entity.SeasonStatus;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Centralized test data for registry-service.
 * All test classes import from here — never build entities inline.
 */
public final class TestFixtures {

    private TestFixtures() {
    }

    public static final UUID ASD_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    public static final UUID SEASON_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    // ── Commands ──────────────────────────────────────────────────────────────

    public static CreateAsdCommand validCreateAsdCommand() {
        return new CreateAsdCommand(
                "12345678901",
                "ASD Test Nuoto",
                "CONI-001",
                "FIN-001",
                "Nuoto",
                "Roma",
                "RM",
                "asd@test.it",
                "+390612345678"
        );
    }

    public static CreateAsdCommand commandWithDuplicateCf() {
        return new CreateAsdCommand(
                "12345678901",
                "ASD Altro Nuoto",
                null, null, null, null, null, null, null
        );
    }

    public static ActivateSeasonCommand validActivateSeasonCommand(UUID asdId) {
        return new ActivateSeasonCommand(
                asdId,
                "2025-2026",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2026, 6, 30)
        );
    }

    public static ActivateSeasonCommand commandWithInvalidDateRange(UUID asdId) {
        return new ActivateSeasonCommand(
                asdId,
                "2025-2026",
                LocalDate.of(2026, 6, 30),
                LocalDate.of(2025, 9, 1)   // fine before inizio
        );
    }

    // ── Entities ──────────────────────────────────────────────────────────────

    public static AsdEntity savedAsd(UUID id) {
        return AsdEntity.builder()
                .id(id)
                .codiceFiscale("12345678901")
                .nome("ASD Test Nuoto")
                .disciplina("Nuoto")
                .stato(AsdStatus.ACTIVE)
                .build();
    }

    public static AsdEntity savedAsd() {
        return savedAsd(UUID.randomUUID());
    }

    public static SeasonEntity savedSeason(UUID id, UUID asdId) {
        return SeasonEntity.builder()
                .id(id)
                .asdId(asdId)
                .codice("2025-2026")
                .dataInizio(LocalDate.of(2025, 9, 1))
                .dataFine(LocalDate.of(2026, 6, 30))
                .stato(SeasonStatus.ACTIVE)
                .build();
    }

    public static SeasonEntity savedSeason() {
        return savedSeason(UUID.randomUUID(), UUID.randomUUID());
    }
}
