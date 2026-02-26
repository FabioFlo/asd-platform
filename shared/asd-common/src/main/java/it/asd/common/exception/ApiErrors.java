package it.asd.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;

/**
 * Central factory for building consistent ProblemDetail error responses.
 * <p>
 * Usage in controllers (inside sealed result switch):
 * <p>
 *   case RenewDocumentResult.NotFound nf ->
 *       ResponseEntity.status(404).body(
 *           ApiErrors.of(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND",
 *               "Document not found: " + nf.documentId()));
 * <p>
 * Error codes are SCREAMING_SNAKE_CASE stable strings — clients use them
 * for i18n and programmatic handling instead of parsing the message text.
 */
public final class ApiErrors {

    private static final String BASE_URL = "https://asd.it/errors/";

    private ApiErrors() {}

    /**
     * Builds a ProblemDetail with a consistent structure:
     *   - type:      <a href="https://asd.it/errors/">...</a>{code-in-kebab-case}
     *   - status:    HTTP status code
     *   - detail:    human-readable message
     *   - code:      stable SCREAMING_SNAKE_CASE string for client logic
     *   - timestamp: current instant
     */
    public static ProblemDetail of(HttpStatus status, String code, String detail) {
        var pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create(BASE_URL + toKebabCase(code)));
        pd.setProperty("code", code);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    /**
     * Overload that accepts extra properties (e.g. field list for validation errors,
     * rule name for business violations).
     * <p>
     * Usage:
     *   ApiErrors.of(BAD_REQUEST, "VALIDATION_FAILED", "Validation failed",
     *       Map.of("fields", List.of("nome: must not be blank")))
     */
    public static ProblemDetail of(HttpStatus status, String code, String detail,
                                   java.util.Map<String, Object> extra) {
        var pd = of(status, code, detail);
        extra.forEach(pd::setProperty);
        return pd;
    }

    // ── Predefined codes ──────────────────────────────────────────────────────
    // Add new constants here as new error cases are introduced across services.
    // Controllers reference these instead of typing raw strings.

    // Generic
    public static final String INTERNAL_ERROR         = "INTERNAL_ERROR";
    public static final String VALIDATION_FAILED      = "VALIDATION_FAILED";
    public static final String NOT_FOUND              = "NOT_FOUND";
    public static final String ALREADY_EXISTS         = "ALREADY_EXISTS";
    public static final String INVALID_DATE_RANGE     = "INVALID_DATE_RANGE";
    public static final String BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION";
    public static final String SERVICE_UNAVAILABLE = "SERVICE_UNAVAILABLE";

    // Compliance
    public static final String DOCUMENT_NOT_FOUND     = "DOCUMENT_NOT_FOUND";
    public static final String PERSON_INELIGIBLE      = "PERSON_INELIGIBLE";
    public static final String COMPLIANCE_UNAVAILABLE = "COMPLIANCE_UNAVAILABLE";

    // Competition
    public static final String ALREADY_REGISTERED     = "ALREADY_REGISTERED";
    public static final String PARTICIPATION_NOT_FOUND = "PARTICIPATION_NOT_FOUND";

    // Registry
    public static final String ASD_NOT_FOUND          = "ASD_NOT_FOUND";
    public static final String SEASON_NOT_FOUND       = "SEASON_NOT_FOUND";
    public static final String ALREADY_ACTIVE_SEASON       = "ALREADY_ACTIVE_SEASON";
    public static final String ALREADY_EXISTS_SEASON       = "ALREADY_EXISTS_SEASON";
    public static final String DUPLICATE_CODICE_FISCALE = "DUPLICATE_CODICE_FISCALE";

    // Identity
    public static final String PERSON_NOT_FOUND       = "PERSON_NOT_FOUND";
    public static final String DUPLICATE_EMAIL        = "DUPLICATE_EMAIL";

    // Membership
    public static final String GROUP_NOT_FOUND        = "GROUP_NOT_FOUND";
    public static final String NOT_A_MEMBER           = "NOT_A_MEMBER";
    public static final String ALREADY_IN_GROUP       = "ALREADY_IN_GROUP";
    public static final String ALREADY_ENROLLED       = "ALREADY_ENROLLED";

    // Scheduling
    public static final String VENUE_NOT_FOUND        = "VENUE_NOT_FOUND";
    public static final String ROOM_NAME_EXISTS        = "ROOM_NAME_EXISTS";
    public static final String ROOM_NOT_FOUND         = "ROOM_NOT_FOUND";
    public static final String TIME_CONFLICT          = "TIME_CONFLICT";

    // Finance
    public static final String PAYMENT_NOT_FOUND      = "PAYMENT_NOT_FOUND";
    public static final String PAYMENT_ALREADY_CONFIRMED = "PAYMENT_ALREADY_CONFIRMED";
    public static final String PAYMENT_ALREADY_CANCELLED = "PAYMENT_ALREADY_CANCELLED";

    // ── Internal helpers ──────────────────────────────────────────────────────

    private static String toKebabCase(String screamingSnakeCase) {
        return screamingSnakeCase.toLowerCase().replace('_', '-');
    }
}