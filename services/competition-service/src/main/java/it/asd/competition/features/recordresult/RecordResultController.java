package it.asd.competition.features.recordresult;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/competition/participants/{participationId}/result")
public class RecordResultController {

    private final RecordResultHandler handler;

    public RecordResultController(RecordResultHandler handler) {
        this.handler = handler;
    }

    @PutMapping
    public ResponseEntity<?> record(
            @PathVariable UUID participationId,
            @Valid @RequestBody RecordResultCommand cmd) {

        var effectiveCmd = new RecordResultCommand(
                participationId, cmd.posizione(), cmd.punteggio(), cmd.resultData());

        return switch (handler.handle(effectiveCmd)) {
            case RecordResultResult.Recorded r ->
                    ResponseEntity.ok(RecordResultResponse.from(
                            // rebuild lightweight response from result fields
                            it.asd.competition.shared.entity.EventParticipationEntity.builder()
                                    .id(r.participationId()).personId(r.personId())
                                    .groupId(r.groupId()).posizione(r.posizione())
                                    .punteggio(r.punteggio()).resultData(r.resultData())
                                    .stato(it.asd.competition.shared.entity.ParticipationStatus.PARTICIPATED)
                                    .build()));

            case RecordResultResult.NotFound nf -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/not-found"));
                pd.setDetail("Participation not found: " + nf.participationId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }
        };
    }
}
