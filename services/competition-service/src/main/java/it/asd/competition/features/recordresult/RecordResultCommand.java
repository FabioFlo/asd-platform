package it.asd.competition.features.recordresult;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record RecordResultCommand(
        @NotNull UUID participationId,
        Integer posizione,
        BigDecimal punteggio,
        Map<String, Object> resultData    // sport-specific JSONB — opaque to the core
) {}
