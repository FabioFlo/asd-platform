package it.asd.registry.features.getcurrentseason;

import java.time.LocalDate;
import java.util.UUID;

public record CurrentSeasonResponse(
        UUID seasonId,
        UUID asdId,
        String codice,
        LocalDate dataInizio,
        LocalDate dataFine
) {}
