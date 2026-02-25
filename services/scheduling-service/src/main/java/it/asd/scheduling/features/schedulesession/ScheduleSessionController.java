package it.asd.scheduling.features.schedulesession;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/scheduling/sessions")
public class ScheduleSessionController {

    private final ScheduleSessionHandler handler;

    public ScheduleSessionController(ScheduleSessionHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> schedule(@Valid @RequestBody ScheduleSessionCommand cmd) {
        return switch (handler.handle(cmd)) {
            case ScheduleSessionResult.Scheduled s -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new SessionResponse(s.sessionId(), cmd.asdId(), cmd.venueId(), cmd.titolo()));

            case ScheduleSessionResult.VenueNotFound v -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/venue-not-found"));
                pd.setDetail("Venue not found or does not belong to ASD");
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }

            case ScheduleSessionResult.RoomNotFound r -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/room-not-found"));
                pd.setDetail("Room not found or does not belong to venue");
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }

            case ScheduleSessionResult.GroupNotFound g -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/group-not-found"));
                pd.setDetail("Group not found in cache — retry in a few seconds");
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }

            case ScheduleSessionResult.TimeConflict t -> {
                var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
                pd.setType(URI.create("https://asd.it/errors/time-conflict"));
                pd.setDetail(t.detail());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
            }

            case ScheduleSessionResult.InvalidTimeRange i -> {
                var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
                pd.setType(URI.create("https://asd.it/errors/invalid-time-range"));
                pd.setDetail("oraFine must be after oraInizio");
                yield ResponseEntity.unprocessableEntity().body(pd);
            }
        };
    }
}
