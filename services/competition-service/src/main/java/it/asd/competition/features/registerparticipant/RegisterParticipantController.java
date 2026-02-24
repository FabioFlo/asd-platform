package it.asd.competition.features.registerparticipant;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@RestController
@RequestMapping("/competition/events/{eventId}/participants")
public class RegisterParticipantController {

    private final RegisterParticipantHandler handler;

    public RegisterParticipantController(RegisterParticipantHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> register(
            @PathVariable UUID eventId,
            @Valid @RequestBody RegisterParticipantCommand cmd) {

        var effectiveCmd = new RegisterParticipantCommand(
                eventId, cmd.asdId(), cmd.seasonId(), cmd.personId(),
                cmd.groupId(), cmd.categoria(), cmd.agonistic());

        // Exhaustive switch — all 4 sealed cases must be handled
        return switch (handler.handle(effectiveCmd)) {

            case RegisterParticipantResult.Registered r ->
                    ResponseEntity.status(HttpStatus.CREATED)
                            .body(new RegistrationResponse(r.participationId(), eventId));

            case RegisterParticipantResult.Ineligible i -> {
                var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
                pd.setType(URI.create("https://asd.it/errors/ineligible"));
                pd.setDetail("Person is not eligible to compete");
                pd.setProperty("blockingDocuments", i.blockingDocuments());
                yield ResponseEntity.unprocessableEntity().body(pd);
            }

            case RegisterParticipantResult.AlreadyRegistered a -> {
                var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
                pd.setType(URI.create("https://asd.it/errors/already-registered"));
                pd.setDetail("Already registered for this event");
                pd.setProperty("existingParticipationId", a.existingParticipationId());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
            }

            // FAIL-CLOSED: Compliance unreachable — inform client to retry
            case RegisterParticipantResult.ComplianceUnavailable u -> {
                var pd = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
                pd.setType(URI.create("https://asd.it/errors/compliance-unavailable"));
                pd.setDetail("Cannot verify eligibility — registration denied. Please retry later.");
                yield ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .header("Retry-After", String.valueOf(Duration.ofMinutes(2).toSeconds()))
                        .body(pd);
            }
        };
    }

    record RegistrationResponse(UUID participationId, UUID eventId) {}
}
