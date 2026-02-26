package it.asd.registry.features.activateseason;

import it.asd.common.exception.ApiErrors;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/registry/asd/{asdId}/seasons")
public class ActivateSeasonController {

    private final ActivateSeasonHandler handler;

    public ActivateSeasonController(ActivateSeasonHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> activate(
            @PathVariable UUID asdId,
            @Valid @RequestBody ActivateSeasonCommand cmd) {

        var effectiveCmd = new ActivateSeasonCommand(
                asdId, cmd.codice(), cmd.dataInizio(), cmd.dataFine());

        return switch (handler.handle(effectiveCmd)) {
            case ActivateSeasonResult.Activated a -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new SeasonResponse(a.seasonId(), asdId, a.codice()));

            case ActivateSeasonResult.AsdNotFound n -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.ASD_NOT_FOUND, "ASD not found: " + n.asdId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }

            case ActivateSeasonResult.AlreadyHasActiveSeason a -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.CONFLICT, ApiErrors.ALREADY_ACTIVE_SEASON, "ASD already has active season: " + a.activeCodice());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
            }

            case ActivateSeasonResult.InvalidDateRange i -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrors.INVALID_DATE_RANGE, i.reason());
                yield ResponseEntity.unprocessableEntity().body(problemDetail);
            }

            case ActivateSeasonResult.DuplicateCodice d -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.CONFLICT, ApiErrors.ALREADY_EXISTS_SEASON, "Season codice already exists: " + d.codice());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
            }
        };
    }
}
