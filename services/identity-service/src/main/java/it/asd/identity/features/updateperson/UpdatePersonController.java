package it.asd.identity.features.updateperson;

import it.asd.common.exception.ApiErrors;
import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/identity/persons/{personId}")
public class UpdatePersonController {

    private final UpdatePersonHandler handler;

    public UpdatePersonController(UpdatePersonHandler handler) {
        this.handler = handler;
    }

    @PatchMapping
    public ResponseEntity<?> update(
            @PathVariable @ValidUUID UUID personId,
            @Valid @RequestBody UpdatePersonCommand cmd) {

        var effectiveCmd = new UpdatePersonCommand(
                personId, cmd.nome(), cmd.cognome(), cmd.email(),
                cmd.telefono(), cmd.indirizzo(), cmd.citta(), cmd.provincia(), cmd.cap());

        return switch (handler.handle(effectiveCmd)) {
            case UpdatePersonResult.Updated _ -> ResponseEntity.ok().build();

            case UpdatePersonResult.NotFound n -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.PERSON_NOT_FOUND, "Person not found: " + n.personId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }

            case UpdatePersonResult.DuplicateEmail e -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrors.DUPLICATE_EMAIL, "Email already in use: " + e.email());
                yield ResponseEntity.unprocessableEntity().body(problemDetail);
            }
        };
    }
}
