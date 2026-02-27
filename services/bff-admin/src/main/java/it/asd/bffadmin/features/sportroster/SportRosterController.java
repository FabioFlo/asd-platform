package it.asd.bffadmin.features.sportroster;

import it.asd.satellite.AdminRoster;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * GET /admin/sport/{disciplina}/roster?asd={asdId}&season={seasonId}
 *
 * Routes to the correct satellite by disciplina.
 */
@RestController
@RequestMapping("/admin/sport/{disciplina}/roster")
public class SportRosterController {

    private final SportRosterHandler handler;

    public SportRosterController(SportRosterHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DIRETTORE','SEGRETARIO','ALLENATORE')")
    public ResponseEntity<AdminRoster> handle(
            @PathVariable String disciplina,
            @RequestParam UUID asdId,
            @RequestParam UUID seasonId) {

        return ResponseEntity.ok(handler.handle(asdId, seasonId, disciplina));
    }
}
