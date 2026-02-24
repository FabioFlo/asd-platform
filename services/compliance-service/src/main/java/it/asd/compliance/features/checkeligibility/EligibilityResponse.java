package it.asd.compliance.features.checkeligibility;

import java.util.List;

/**
 * HTTP response record.
 * Static factory maps from the sealed EligibilityResult.
 *
 * 'eligible' is the single boolean the Competition service checks
 * for its fail-closed decision. The other fields provide detail for UIs.
 */
public record EligibilityResponse(
        boolean      eligible,
        List<String> blockingDocuments,
        List<String> warnings
) {
    public static EligibilityResponse from(EligibilityResult result) {
        return switch (result) {
            case EligibilityResult.Eligible e ->
                    new EligibilityResponse(true, List.of(), List.of());

            case EligibilityResult.ExpiringSoon e ->
                    new EligibilityResponse(true, List.of(), e.expiringDocuments());

            case EligibilityResult.Ineligible e ->
                    new EligibilityResponse(false, e.blockingDocuments(), List.of());
        };
    }
}
