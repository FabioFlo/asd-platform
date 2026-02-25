package it.asd.membership.features.addtogroup;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/membership/groups/{groupId}/members")
public class AddToGroupController {

    private final AddToGroupHandler handler;

    public AddToGroupController(AddToGroupHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> add(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddToGroupCommand cmd) {

        var effectiveCmd = new AddToGroupCommand(
                cmd.personId(), groupId, cmd.seasonId(), cmd.ruolo(), cmd.dataIngresso());

        return switch (handler.handle(effectiveCmd)) {
            case AddToGroupResult.Added a -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new EnrollmentResponse(a.enrollmentId(), groupId, cmd.personId()));

            case AddToGroupResult.GroupNotFound g -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/group-not-found"));
                pd.setDetail("Group not found: " + g.groupId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }

            case AddToGroupResult.NotAMember n -> {
                var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
                pd.setType(URI.create("https://asd.it/errors/not-a-member"));
                pd.setDetail(n.detail());
                yield ResponseEntity.unprocessableEntity().body(pd);
            }

            case AddToGroupResult.AlreadyInGroup a -> {
                var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
                pd.setType(URI.create("https://asd.it/errors/already-in-group"));
                pd.setDetail("Person already in this group with enrollmentId: " + a.existingId());
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
            }
        };
    }
}
