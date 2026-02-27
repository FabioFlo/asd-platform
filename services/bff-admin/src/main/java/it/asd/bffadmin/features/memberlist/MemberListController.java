package it.asd.bffadmin.features.memberlist;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/admin/members")
public class MemberListController {

    private final MemberListHandler handler;

    public MemberListController(MemberListHandler handler) {
        this.handler = handler;
    }

    /**
     * GET /admin/members?asd=&season=&page=&size=&search=
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DIRETTORE', 'SEGRETARIO', 'ALLENATORE')")
    public ResponseEntity<MemberListView> list(
            @RequestParam UUID asd,
            @RequestParam UUID season,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false)    String search) {

        return ResponseEntity.ok(
                handler.handle(new MemberListQuery(asd, season, page, size, search)));
    }
}
