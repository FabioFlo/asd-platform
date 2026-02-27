package it.asd.membership.features.addtogroup;

import it.asd.common.exception.ApiErrors;
import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/membership/groups/{groupId}/members")
public class AddToGroupController {

    private final AddToGroupHandler handler;

    public AddToGroupController(AddToGroupHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> add(
            @PathVariable @ValidUUID UUID groupId,
            @Valid @RequestBody AddToGroupCommand cmd) {

        var effectiveCmd = new AddToGroupCommand(
                cmd.personId(), groupId, cmd.seasonId(), cmd.ruolo(), cmd.dataIngresso());

        return switch (handler.handle(effectiveCmd)) {
            case AddToGroupResult.Added a -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new EnrollmentResponse(a.enrollmentId(), groupId, cmd.personId()));

            case AddToGroupResult.GroupNotFound g -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.GROUP_NOT_FOUND, "Group not found: " + g.groupId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }

            case AddToGroupResult.NotAMember n -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrors.NOT_A_MEMBER, "Not a member: " + n.detail());
                yield ResponseEntity.unprocessableEntity().body(problemDetail);
            }

            case AddToGroupResult.AlreadyInGroup a -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.CONFLICT, ApiErrors.ALREADY_IN_GROUP, "Person already in this group with enrollmentId: " + a.existingId());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
            }
        };
    }
}
