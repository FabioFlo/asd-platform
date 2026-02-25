package it.asd.identity.features.addqualification;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/identity/persons/{personId}/qualifications")
public class AddQualificationController {

    private final AddQualificationHandler handler;

    public AddQualificationController(AddQualificationHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> add(
            @PathVariable UUID personId,
            @Valid @RequestBody AddQualificationCommand cmd) {

        var effectiveCmd = new AddQualificationCommand(
                personId, cmd.tipo(), cmd.ente(), cmd.livello(),
                cmd.dataConseguimento(), cmd.dataScadenza(), cmd.numeroPatentino(), cmd.note());

        return switch (handler.handle(effectiveCmd)) {
            case AddQualificationResult.Added a -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new QualificationResponse(a.qualificationId(), personId));

            case AddQualificationResult.PersonNotFound n -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/person-not-found"));
                pd.setDetail("Person not found: " + n.personId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }
        };
    }
}
