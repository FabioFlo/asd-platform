package it.asd.bffadmin.features.compliancealerts;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ComplianceAlerts controller — thin HTTP adapter.
 * TODO: replace Object with typed Query/View records.
 */
@RestController
@RequestMapping("/admin/compliance/alerts")
public class ComplianceAlertsController {

    private final ComplianceAlertsHandler handler;

    public ComplianceAlertsController(ComplianceAlertsHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DIRETTORE','SEGRETARIO')")
    public ResponseEntity<Object> handle() {
        // TODO: extract params, call handler
        return ResponseEntity.ok(null);
    }
}
