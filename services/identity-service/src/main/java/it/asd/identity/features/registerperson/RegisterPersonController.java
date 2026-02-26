package it.asd.identity.features.registerperson;

import it.asd.common.exception.ApiErrors;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/identity/persons")
public class RegisterPersonController {

    private final RegisterPersonHandler handler;

    public RegisterPersonController(RegisterPersonHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> register(@Valid @RequestBody RegisterPersonCommand cmd) {
        return switch (handler.handle(cmd)) {
            case RegisterPersonResult.Registered r -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new PersonResponse(r.personId(), cmd.codiceFiscale(),
                            cmd.nome(), cmd.cognome(), cmd.dataNascita(), cmd.email(), "ACTIVE"));

            case RegisterPersonResult.DuplicateCodiceFiscale d -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrors.ALREADY_REGISTERED, "Codice fiscale already registered: " + d.cf());
                yield ResponseEntity.unprocessableEntity().body(problemDetail);
            }

            case RegisterPersonResult.DuplicateEmail e -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrors.DUPLICATE_EMAIL, "Email already registered: " + e.email());
                yield ResponseEntity.unprocessableEntity().body(problemDetail);
            }
        };
    }
}
