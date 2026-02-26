package it.asd.membership.features.creategroup;

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
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrors.ALREADY_EXISTS, "Group name already exists in this season: " + d.nome());
/*                var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
                pd.setType(URI.create("https://asd.it/errors/duplicate-group-name"));
                pd.setDetail("Group name already exists in this season: " + d.nome());*/
                yield ResponseEntity.unprocessableEntity().body(problemDetail);
            }
        };
    }
}
