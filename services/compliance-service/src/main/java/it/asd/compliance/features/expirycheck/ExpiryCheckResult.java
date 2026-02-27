package it.asd.compliance.features.expirycheck;

import java.util.List;
import java.util.UUID;

/**
 * Sealed result for the daily expiry scan job.
 * Always returns Summary (the job always completes — errors are per-document).
 * Using sealed here documents the fact that there's only one outcome shape,
 * making future extension (e.g. adding PartialFailure) explicit and safe.
 */
public sealed interface ExpiryCheckResult
        permits ExpiryCheckResult.Summary {

    record Summary(
            int expiredCount,
            int expiringSoonCount,
            List<UUID> failedDocumentIds    // IDs that couldn't be processed
    ) implements ExpiryCheckResult {
        public boolean hasFailures() {
            return !failedDocumentIds.isEmpty();
        }
    }
}
