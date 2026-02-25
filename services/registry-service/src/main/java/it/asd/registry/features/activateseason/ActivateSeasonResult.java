package it.asd.registry.features.activateseason;

import java.util.UUID;

public sealed interface ActivateSeasonResult
        permits ActivateSeasonResult.Activated,
                ActivateSeasonResult.AsdNotFound,
                ActivateSeasonResult.AlreadyHasActiveSeason,
                ActivateSeasonResult.InvalidDateRange,
                ActivateSeasonResult.DuplicateCodice {

    record Activated(UUID seasonId, String codice) implements ActivateSeasonResult {}
    record AsdNotFound(UUID asdId) implements ActivateSeasonResult {}
    record AlreadyHasActiveSeason(String activeCodice) implements ActivateSeasonResult {}
    record InvalidDateRange(String reason) implements ActivateSeasonResult {}
    record DuplicateCodice(String codice) implements ActivateSeasonResult {}
}
