package it.asd.bffmember.features.schedule;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Schedule controller — thin HTTP adapter.
 * TODO: replace Object with typed Query/View records.
 */
@RestController
@RequestMapping("/member/schedule")
public class ScheduleController {

    private final ScheduleHandler handler;

    public ScheduleController(ScheduleHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    @PreAuthorize("hasRole('ATLETA')")
    public ResponseEntity<Object> handle() {
        // TODO: extract params, call handler
        return ResponseEntity.ok(null);
    }
}
