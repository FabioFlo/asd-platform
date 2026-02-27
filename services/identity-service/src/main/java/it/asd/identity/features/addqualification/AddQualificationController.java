package it.asd.identity.features.addqualification;

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
@RequestMapping("/identity/persons/{personId}/qualifications")
public class AddQualificationController {

    private final AddQualificationHandler handler;

    public AddQualificationController(AddQualificationHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> add(
            @PathVariable @ValidUUID UUID personId,
            @Valid @RequestBody AddQualificationCommand cmd) {

        var effectiveCmd = new AddQualificationCommand(
                personId, cmd.tipo(), cmd.ente(), cmd.livello(),
                cmd.dataConseguimento(), cmd.dataScadenza(), cmd.numeroPatentino(), cmd.note());

        return switch (handler.handle(effectiveCmd)) {
            case AddQualificationResult.Added a -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new QualificationResponse(a.qualificationId(), personId));

            case AddQualificationResult.PersonNotFound n -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.PERSON_NOT_FOUND, "Person not found: " + n.personId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }
        };
    }
}
