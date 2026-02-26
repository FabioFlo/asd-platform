package it.asd.scheduling.features.createvenue;

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
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrors.ROOM_NAME_EXISTS, "Venue name already exists for this ASD: " + d.nome());
                yield ResponseEntity.unprocessableEntity().body(problemDetail);
            }
        };
    }
}
