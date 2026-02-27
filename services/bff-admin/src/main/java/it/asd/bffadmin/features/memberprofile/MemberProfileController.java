package it.asd.bffadmin.features.memberprofile;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * MemberProfile controller — thin HTTP adapter.
 * TODO: replace Object with typed Query/View records.
 */
@RestController
@RequestMapping("/admin/members/{id}")
public class MemberProfileController {

    private final MemberProfileHandler handler;

    public MemberProfileController(MemberProfileHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DIRETTORE','SEGRETARIO','ALLENATORE')")
    public ResponseEntity<Object> handle() {
        // TODO: extract params, call handler
        return ResponseEntity.ok(null);
    }
}
