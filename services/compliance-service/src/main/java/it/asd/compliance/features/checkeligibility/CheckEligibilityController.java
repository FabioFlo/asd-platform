package it.asd.compliance.features.checkeligibility;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * GET /compliance/persons/{personId}/eligibility
 *
 * Called synchronously by competition-service before allowing event registration.
 * FAIL-CLOSED: if this endpoint is down, competition-service must deny registration.
 *
 * Returns 200 regardless of eligibility (eligible: true/false in body).
 * Returns 5xx only on unexpected errors → triggers fail-closed in the caller.
 */
@RestController
@RequestMapping("/compliance/persons/{personId}/eligibility")
public class CheckEligibilityController {

    private final CheckEligibilityHandler handler;

    public CheckEligibilityController(CheckEligibilityHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    public ResponseEntity<EligibilityResponse> check(
            @PathVariable UUID personId,
            @RequestParam  UUID asdId,
            @RequestParam(defaultValue = "true") boolean agonistic) {

        var result   = handler.handle(new CheckEligibilityQuery(personId, asdId, agonistic));
        var response = EligibilityResponse.from(result);
        return ResponseEntity.ok(response);
    }
}
