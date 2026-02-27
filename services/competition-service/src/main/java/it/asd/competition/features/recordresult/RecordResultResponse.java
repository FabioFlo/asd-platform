package it.asd.competition.features.recordresult;

import it.asd.competition.shared.entity.EventParticipationEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP response. Static factory — no MapStruct needed for this flat shape.
 * MapStruct IS used in RecordResultMapper for the entity → Recorded mapping
 * because it handles the nested resultData Map safely with null checks.
 */
public record RecordResultResponse(
        UUID participationId,
        UUID personId,
        UUID groupId,
        Integer posizione,
        BigDecimal punteggio,
        Map<String, Object> resultData,
        String stato
) {
    public static RecordResultResponse from(EventParticipationEntity e) {
        return new RecordResultResponse(
                e.getId(), e.getPersonId(), e.getGroupId(),
                e.getPosizione(), e.getPunteggio(),
                e.getResultData(), e.getStato().name());
    }
}
