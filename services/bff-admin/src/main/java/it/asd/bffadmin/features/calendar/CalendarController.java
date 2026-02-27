package it.asd.bffadmin.features.calendar;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Calendar controller — thin HTTP adapter.
 * TODO: replace Object with typed Query/View records.
 */
@RestController
@RequestMapping("/admin/calendar")
public class CalendarController {

    private final CalendarHandler handler;

    public CalendarController(CalendarHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DIRETTORE','SEGRETARIO','ALLENATORE')")
    public ResponseEntity<Object> handle() {
        // TODO: extract params, call handler
        return ResponseEntity.ok(null);
    }
}
