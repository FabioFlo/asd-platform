package it.asd.bffmember.features.payments;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Payments controller — thin HTTP adapter.
 * TODO: replace Object with typed Query/View records.
 */
@RestController
@RequestMapping("/member/payments")
public class PaymentsController {

    private final PaymentsHandler handler;

    public PaymentsController(PaymentsHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    @PreAuthorize("hasRole('ATLETA')")
    public ResponseEntity<Object> handle() {
        // TODO: extract params, call handler
        return ResponseEntity.ok(null);
    }
}
