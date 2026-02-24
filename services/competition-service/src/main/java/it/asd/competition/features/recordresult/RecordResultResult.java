package it.asd.competition.features.recordresult;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * Sealed result for RecordResult.
 * ResultData is returned so sport satellite services can receive
 * the full snapshot via the ParticipantResultSetEvent.
 */
public sealed interface RecordResultResult
        permits RecordResultResult.Recorded,
                RecordResultResult.NotFound {

    record Recorded(
            UUID participationId,
            UUID personId,
            UUID groupId,
            String disciplina,
            Integer posizione,
            BigDecimal punteggio,
            Map<String, Object> resultData
    ) implements RecordResultResult {}

    record NotFound(UUID participationId) implements RecordResultResult {}
}
