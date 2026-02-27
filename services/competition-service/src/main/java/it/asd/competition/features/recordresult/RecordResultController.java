package it.asd.competition.features.recordresult;

import it.asd.common.exception.ApiErrors;
import it.asd.common.validation.annotation.ValidUUID;
import it.asd.competition.shared.entity.EventParticipationEntity;
import it.asd.competition.shared.entity.ParticipationStatus;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/competition/participants/{participationId}/result")
public class RecordResultController {

    private final RecordResultHandler handler;

    public RecordResultController(RecordResultHandler handler) {
        this.handler = handler;
    }

    @PutMapping
    public ResponseEntity<?> record(
            @PathVariable @ValidUUID UUID participationId,
            @Valid @RequestBody RecordResultCommand cmd) {

        var effectiveCmd = new RecordResultCommand(
                participationId, cmd.posizione(), cmd.punteggio(), cmd.resultData());

        return switch (handler.handle(effectiveCmd)) {
            case RecordResultResult.Recorded r -> ResponseEntity.ok(RecordResultResponse.from(
                    // rebuild lightweight response from result fields
                    EventParticipationEntity.builder()
                            .id(r.participationId()).personId(r.personId())
                            .groupId(r.groupId()).posizione(r.posizione())
                            .punteggio(r.punteggio()).resultData(r.resultData())
                            .stato(ParticipationStatus.PARTICIPATED)
                            .build()));

            case RecordResultResult.NotFound nf -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.PARTICIPATION_NOT_FOUND, nf.participationId().toString());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }
        };
    }
}
