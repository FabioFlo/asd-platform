package it.asd.membership.features.creategroup;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/membership/groups")
public class CreateGroupController {

    private final CreateGroupHandler handler;

    public CreateGroupController(CreateGroupHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateGroupCommand cmd) {
        return switch (handler.handle(cmd)) {
            case CreateGroupResult.Created c -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new GroupResponse(c.groupId(), cmd.asdId(), cmd.seasonId(), cmd.nome()));

            case CreateGroupResult.DuplicateName d -> {
                var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
                pd.setType(URI.create("https://asd.it/errors/duplicate-group-name"));
                pd.setDetail("Group name already exists in this season: " + d.nome());
                yield ResponseEntity.unprocessableEntity().body(pd);
            }
        };
    }
}
