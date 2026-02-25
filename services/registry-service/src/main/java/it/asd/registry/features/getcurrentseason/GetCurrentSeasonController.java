package it.asd.registry.features.getcurrentseason;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/registry/asd/{asdId}/season/current")
public class GetCurrentSeasonController {

    private final GetCurrentSeasonHandler handler;

    public GetCurrentSeasonController(GetCurrentSeasonHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    public ResponseEntity<?> getCurrent(@PathVariable UUID asdId) {
        return switch (handler.handle(new GetCurrentSeasonQuery(asdId))) {
            case GetCurrentSeasonResult.Found f -> ResponseEntity.ok(
                    new CurrentSeasonResponse(f.seasonId(), asdId, f.codice(),
                            f.dataInizio(), f.dataFine()));

            case GetCurrentSeasonResult.NoActiveSeason n -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/no-active-season"));
                pd.setDetail("No active season for ASD: " + n.asdId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }
        };
    }
}
