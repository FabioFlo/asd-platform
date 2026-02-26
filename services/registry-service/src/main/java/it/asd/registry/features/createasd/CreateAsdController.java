package it.asd.registry.features.createasd;

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
@RequestMapping("/registry/asd")
public class CreateAsdController {

    private final CreateAsdHandler handler;

    public CreateAsdController(CreateAsdHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateAsdCommand cmd) {
        return switch (handler.handle(cmd)) {
            case CreateAsdResult.Created c -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new AsdResponse(c.asdId(), cmd.codiceFiscale(), cmd.nome(),
                            cmd.disciplina(), "ACTIVE"));

            case CreateAsdResult.DuplicateCodiceFiscale d -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrors.DUPLICATE_CODICE_FISCALE, "Codice fiscale already registered: " + d.cf());
                yield ResponseEntity.unprocessableEntity().body(problemDetail);
            }
        };
    }
}
