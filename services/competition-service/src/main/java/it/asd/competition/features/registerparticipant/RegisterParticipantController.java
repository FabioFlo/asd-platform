package it.asd.competition.features.registerparticipant;

import it.asd.common.exception.ApiErrors;
import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Validated
@RestController
@RequestMapping("/competition/events/{eventId}/participants")
public class RegisterParticipantController {

    private final RegisterParticipantHandler handler;

    public RegisterParticipantController(RegisterParticipantHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> register(
            @PathVariable @ValidUUID UUID eventId,
            @Valid @RequestBody RegisterParticipantCommand cmd) {

        var effectiveCmd = new RegisterParticipantCommand(
                eventId, cmd.asdId(), cmd.seasonId(), cmd.personId(),
                cmd.groupId(), cmd.categoria(), cmd.agonistic());

        // Exhaustive switch — all 4 sealed cases must be handled
        return switch (handler.handle(effectiveCmd)) {

            case RegisterParticipantResult.Registered r -> ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegistrationResponse(r.participationId(), eventId));

            case RegisterParticipantResult.Ineligible _ -> {
                ProblemDetail problemDetail = ApiErrors.of(UNPROCESSABLE_ENTITY, ApiErrors.PERSON_INELIGIBLE, "Person is not eligible to compete");
                yield ResponseEntity.unprocessableEntity().body(problemDetail);
            }

            case RegisterParticipantResult.AlreadyRegistered _ -> {
                ProblemDetail problemDetail = ApiErrors.of(CONFLICT, ApiErrors.ALREADY_REGISTERED, "Already registered");
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
            }

            // FAIL-CLOSED: Compliance unreachable — inform client to retry
            case RegisterParticipantResult.ComplianceUnavailable _ -> {
                ProblemDetail problemDetail = ApiErrors.of(SERVICE_UNAVAILABLE, ApiErrors.SERVICE_UNAVAILABLE, "Cannot verify eligibility — registration denied. Please retry later.");
                yield ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .header("Retry-After", String.valueOf(Duration.ofMinutes(2).toSeconds()))
                        .body(problemDetail);
            }
        };
    }

    record RegistrationResponse(UUID participationId, UUID eventId) {
    }
}
