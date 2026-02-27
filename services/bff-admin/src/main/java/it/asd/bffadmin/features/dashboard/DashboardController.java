package it.asd.bffadmin.features.dashboard;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Dashboard controller — thin HTTP adapter.
 * No business logic here. Resolves query params, delegates to handler,
 * maps response to HTTP.
 */
@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    private final DashboardHandler handler;

    public DashboardController(DashboardHandler handler) {
        this.handler = handler;
    }

    /**
     * GET /admin/dashboard?asd={asdId}&season={seasonId}
     *
     * Returns 200 always — partial data is signalled inside the body
     * via partialData=true. The Vue SPA renders available sections
     * and shows placeholder notices for unavailable ones.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('DIRETTORE', 'SEGRETARIO', 'ALLENATORE')")
    public ResponseEntity<DashboardView> getDashboard(
            @RequestParam UUID asd,
            @RequestParam UUID season) {

        var view = handler.handle(new DashboardQuery(asd, season));
        return ResponseEntity.ok(view);
    }
}
