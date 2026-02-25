package it.asd.scheduling.features.createvenue;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/scheduling/venues")
public class CreateVenueController {

    private final CreateVenueHandler handler;

    public CreateVenueController(CreateVenueHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateVenueCommand cmd) {
        return switch (handler.handle(cmd)) {
            case CreateVenueResult.Created c -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new VenueResponse(c.venueId(), cmd.asdId(), cmd.nome()));

            case CreateVenueResult.DuplicateName d -> {
                var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
                pd.setType(URI.create("https://asd.it/errors/duplicate-venue-name"));
                pd.setDetail("Venue name already exists for this ASD: " + d.nome());
                yield ResponseEntity.unprocessableEntity().body(pd);
            }
        };
    }
}
