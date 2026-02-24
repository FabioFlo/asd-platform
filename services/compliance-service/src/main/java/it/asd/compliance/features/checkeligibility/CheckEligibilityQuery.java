package it.asd.compliance.features.checkeligibility;

import java.util.UUID;

/**
 * Query record (read-only — no @Transactional write).
 * 'agonistic' drives which document set is checked:
 *   true  → DocumentType.AGONISTIC_REQUIRED
 *   false → DocumentType.RECREATIONAL_REQUIRED
 */
public record CheckEligibilityQuery(
        UUID personId,
        UUID asdId,
        boolean agonistic
) {}
