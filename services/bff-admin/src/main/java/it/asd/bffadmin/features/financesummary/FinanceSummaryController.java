package it.asd.bffadmin.features.financesummary;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * FinanceSummary controller — thin HTTP adapter.
 * TODO: replace Object with typed Query/View records.
 */
@RestController
@RequestMapping("/admin/finance/summary")
public class FinanceSummaryController {

    private final FinanceSummaryHandler handler;

    public FinanceSummaryController(FinanceSummaryHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DIRETTORE','SEGRETARIO')")
    public ResponseEntity<Object> handle() {
        // TODO: extract params, call handler
        return ResponseEntity.ok(null);
    }
}
