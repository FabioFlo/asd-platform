package it.asd.bffmember.features.passport;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Passport controller — thin HTTP adapter.
 * TODO: replace Object with typed Query/View records.
 */
@RestController
@RequestMapping("/member/passport")
public class PassportController {

    private final PassportHandler handler;

    public PassportController(PassportHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    @PreAuthorize("hasRole('ATLETA')")
    public ResponseEntity<Object> handle() {
        // TODO: extract params, call handler
        return ResponseEntity.ok(null);
    }
}
