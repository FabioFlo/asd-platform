package it.asd.membership.features.enrollmember;

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
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.CONFLICT, ApiErrors.ALREADY_ENROLLED, "Person already enrolled with membershipId: " + a.existingId());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
            }

            case EnrollMemberResult.PersonNotFound n -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.PERSON_NOT_FOUND, "Person not found: " + n.personId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }

            case EnrollMemberResult.AsdNotFound a -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.ASD_NOT_FOUND, "Asd not found: " + a.asdId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }

            case EnrollMemberResult.SeasonNotFound s -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.SEASON_NOT_FOUND, "No active season for ASD: " + s.asdId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }
        };
    }
}
