package it.asd.membership.features.enrollmember;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/membership/members")
public class EnrollMemberController {

    private final EnrollMemberHandler handler;

    public EnrollMemberController(EnrollMemberHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> enroll(@Valid @RequestBody EnrollMemberCommand cmd) {
        return switch (handler.handle(cmd)) {
            case EnrollMemberResult.Enrolled e -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new MembershipResponse(e.membershipId(), e.numeroTessera()));

            case EnrollMemberResult.AlreadyEnrolled a -> {
                var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
                pd.setType(URI.create("https://asd.it/errors/already-enrolled"));
                pd.setDetail("Person already enrolled with membershipId: " + a.existingId());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
            }

            case EnrollMemberResult.PersonNotFound n -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/person-not-found"));
                pd.setDetail("Person not found: " + n.personId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }

            case EnrollMemberResult.AsdNotFound a -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/asd-not-found"));
                pd.setDetail("ASD not found: " + a.asdId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }

            case EnrollMemberResult.SeasonNotFound s -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/season-not-found"));
                pd.setDetail("No active season for ASD: " + s.asdId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }
        };
    }
}
