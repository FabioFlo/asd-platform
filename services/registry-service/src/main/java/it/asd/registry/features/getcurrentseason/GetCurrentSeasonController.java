package it.asd.registry.features.getcurrentseason;

import it.asd.common.exception.ApiErrors;
import it.asd.common.validation.annotation.ValidUUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/registry/asd/{asdId}/season/current")
public class GetCurrentSeasonController {

    private final GetCurrentSeasonHandler handler;

    public GetCurrentSeasonController(GetCurrentSeasonHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    public ResponseEntity<?> getCurrent(@PathVariable @ValidUUID UUID asdId) {
        return switch (handler.handle(new GetCurrentSeasonQuery(asdId))) {
            case GetCurrentSeasonResult.Found f -> ResponseEntity.ok(
                    new CurrentSeasonResponse(f.seasonId(), asdId, f.codice(),
                            f.dataInizio(), f.dataFine()));

            case GetCurrentSeasonResult.NoActiveSeason _ -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.DUPLICATE_CODICE_FISCALE, "No active season for ASD: " + asdId);
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }
        };
    }
}
