package it.asd.scheduling.features.schedulesession;

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

            case ScheduleSessionResult.VenueNotFound _ -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.VENUE_NOT_FOUND, "Venue not found or does not belong to ASD");
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }

            case ScheduleSessionResult.RoomNotFound _ -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.ROOM_NOT_FOUND, "Room not found or does not belong to venue");
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }

            case ScheduleSessionResult.GroupNotFound _ -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.GROUP_NOT_FOUND, "Group not found in cache — retry in a few seconds");
               /* var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/group-not-found"));
                pd.setDetail("Group not found in cache — retry in a few seconds");*/
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }

            case ScheduleSessionResult.TimeConflict _ -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.TIME_CONFLICT, "Time conflict");
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
            }

            case ScheduleSessionResult.InvalidTimeRange _ -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.INVALID_DATE_RANGE, "oraFine must be after oraInizio");
                yield ResponseEntity.unprocessableEntity().body(problemDetail);
            }
        };
    }
}
