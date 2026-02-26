package it.asd.identity.features.getperson;

import it.asd.common.exception.ApiErrors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.PERSON_NOT_FOUND, "Person not found: " + n.personId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }
        };
    }
}
