package it.asd.registry.features.activateseason;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/asd-not-found"));
                pd.setDetail("ASD not found: " + n.asdId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }

            case ActivateSeasonResult.AlreadyHasActiveSeason a -> {
                var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
                pd.setType(URI.create("https://asd.it/errors/already-has-active-season"));
                pd.setDetail("ASD already has active season: " + a.activeCodice());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
            }

            case ActivateSeasonResult.InvalidDateRange i -> {
                var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
                pd.setType(URI.create("https://asd.it/errors/invalid-date-range"));
                pd.setDetail(i.reason());
                yield ResponseEntity.unprocessableEntity().body(pd);
            }

            case ActivateSeasonResult.DuplicateCodice d -> {
                var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
                pd.setType(URI.create("https://asd.it/errors/duplicate-codice"));
                pd.setDetail("Season codice already exists: " + d.codice());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
            }
        };
    }
}
