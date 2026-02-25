package it.asd.identity.features.registerperson;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
                var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
                pd.setType(URI.create("https://asd.it/errors/duplicate-codice-fiscale"));
                pd.setDetail("Codice fiscale already registered: " + d.cf());
                yield ResponseEntity.unprocessableEntity().body(pd);
            }

            case RegisterPersonResult.DuplicateEmail e -> {
                var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
                pd.setType(URI.create("https://asd.it/errors/duplicate-email"));
                pd.setDetail("Email already registered: " + e.email());
                yield ResponseEntity.unprocessableEntity().body(pd);
            }
        };
    }
}
