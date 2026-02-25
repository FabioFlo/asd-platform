package it.asd.registry.features.getcurrentseason;

import java.time.LocalDate;
import java.util.UUID;

public sealed interface GetCurrentSeasonResult
        permits GetCurrentSeasonResult.Found, GetCurrentSeasonResult.NoActiveSeason {

    record Found(UUID seasonId, String codice, LocalDate dataInizio, LocalDate dataFine)
            implements GetCurrentSeasonResult {}

    record NoActiveSeason(UUID asdId) implements GetCurrentSeasonResult {}
}
