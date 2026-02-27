package it.asd.compliance.features.checkeligibility;

import java.util.List;

/**
 * Sealed result for eligibility checks.
 * <p>
 * Three possible outcomes, each with exactly the data it needs:
 * - Eligible:    all required documents are valid
 * - Ineligible:  one or more documents are missing/expired (FAIL-CLOSED trigger)
 * - ExpiringSoon: eligible today but documents expiring within 30 days (warning)
 * <p>
 * Note: ExpiringSoon extends Eligible — the person CAN compete but needs a warning.
 */
public sealed interface EligibilityResult
        permits EligibilityResult.Eligible,
        EligibilityResult.ExpiringSoon,
        EligibilityResult.Ineligible {

    /**
     * All required docs are valid and not near expiry. Clear to compete.
     */
    record Eligible() implements EligibilityResult {
    }

    /**
     * All required docs are valid but one or more expire within EXPIRY_WARNING_DAYS.
     * Still eligible to compete — but the UI should warn the user.
     */
    record ExpiringSoon(List<String> expiringDocuments) implements EligibilityResult {
        public boolean isEligible() {
            return true;
        }
    }

    /**
     * One or more required documents are missing or expired.
     * FAIL-CLOSED: competition service MUST deny registration when it receives this.
     * blockingDocuments contains type + reason for each blocker (for UI messages).
     */
    record Ineligible(List<String> blockingDocuments) implements EligibilityResult {
        public boolean isEligible() {
            return false;
        }
    }
}
