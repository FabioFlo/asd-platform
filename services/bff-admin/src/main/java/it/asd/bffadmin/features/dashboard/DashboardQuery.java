package it.asd.bffadmin.features.dashboard;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Query — immutable input for the Dashboard feature.
 * Carries the ASD context resolved from the JWT + query params.
 */
public record DashboardQuery(
        @NotNull UUID asdId,
        @NotNull UUID seasonId
) {}
