package it.asd.bffmember.features.competitions;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Competitions controller — thin HTTP adapter.
 * TODO: replace Object with typed Query/View records.
 */
@RestController
@RequestMapping("/member/competitions")
public class CompetitionsController {

    private final CompetitionsHandler handler;

    public CompetitionsController(CompetitionsHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    @PreAuthorize("hasRole('ATLETA')")
    public ResponseEntity<Object> handle() {
        // TODO: extract params, call handler
        return ResponseEntity.ok(null);
    }
}
