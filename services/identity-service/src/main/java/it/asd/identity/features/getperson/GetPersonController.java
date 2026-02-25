package it.asd.identity.features.getperson;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/identity/persons/{personId}")
public class GetPersonController {

    private final GetPersonHandler handler;

    public GetPersonController(GetPersonHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    public ResponseEntity<?> get(@PathVariable UUID personId) {
        return switch (handler.handle(new GetPersonQuery(personId))) {
            case GetPersonResult.Found f -> ResponseEntity.ok(f.response());

            case GetPersonResult.NotFound n -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/person-not-found"));
                pd.setDetail("Person not found: " + n.personId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }
        };
    }
}
