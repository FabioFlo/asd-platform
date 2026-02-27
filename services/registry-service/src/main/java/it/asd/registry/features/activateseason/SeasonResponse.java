package it.asd.registry.features.activateseason;

import java.util.UUID;

public record SeasonResponse(UUID seasonId, UUID asdId, String codice) {
}
