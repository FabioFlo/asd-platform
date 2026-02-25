package it.asd.identity.features.updateperson;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/identity/persons/{personId}")
public class UpdatePersonController {

    private final UpdatePersonHandler handler;

    public UpdatePersonController(UpdatePersonHandler handler) {
        this.handler = handler;
    }

    @PatchMapping
    public ResponseEntity<?> update(
            @PathVariable UUID personId,
            @Valid @RequestBody UpdatePersonCommand cmd) {

        var effectiveCmd = new UpdatePersonCommand(
                personId, cmd.nome(), cmd.cognome(), cmd.email(),
                cmd.telefono(), cmd.indirizzo(), cmd.citta(), cmd.provincia(), cmd.cap());

        return switch (handler.handle(effectiveCmd)) {
            case UpdatePersonResult.Updated u -> ResponseEntity.ok().build();

            case UpdatePersonResult.NotFound n -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/person-not-found"));
                pd.setDetail("Person not found: " + n.personId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }

            case UpdatePersonResult.DuplicateEmail e -> {
                var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
                pd.setType(URI.create("https://asd.it/errors/duplicate-email"));
                pd.setDetail("Email already in use: " + e.email());
                yield ResponseEntity.unprocessableEntity().body(pd);
            }
        };
    }
}
