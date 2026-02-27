package it.asd.competition.features.recordresult;

import it.asd.common.validation.annotation.ValidUUID;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record RecordResultCommand(
        @ValidUUID UUID participationId,
        Integer posizione,
        BigDecimal punteggio,
        Map<String, Object> resultData    // sport-specific JSONB — opaque to the core
) {
}
