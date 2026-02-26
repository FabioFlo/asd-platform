package it.asd.scheduling.features.addroom;

import it.asd.common.exception.ApiErrors;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/scheduling/venues/{venueId}/rooms")
public class AddRoomController {

    private final AddRoomHandler handler;

    public AddRoomController(AddRoomHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> add(
            @PathVariable UUID venueId,
            @Valid @RequestBody AddRoomCommand cmd) {

        var effectiveCmd = new AddRoomCommand(venueId, cmd.nome(), cmd.capienza(), cmd.note());

        return switch (handler.handle(effectiveCmd)) {
            case AddRoomResult.Added a -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new RoomResponse(a.roomId(), venueId, cmd.nome()));

            case AddRoomResult.VenueNotFound v -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.VENUE_NOT_FOUND, "No active venue: " + v.venueId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }

            case AddRoomResult.DuplicateName d -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrors.ROOM_NAME_EXISTS, "Room name already exists in this venue: " + d.nome());
                yield ResponseEntity.unprocessableEntity().body(problemDetail);
            }
        };
    }
}
