package it.asd.registry.features.createasd;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

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
                var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
                pd.setType(URI.create("https://asd.it/errors/duplicate-codice-fiscale"));
                pd.setDetail("Codice fiscale already registered: " + d.cf());
                yield ResponseEntity.unprocessableEntity().body(pd);
            }
        };
    }
}
