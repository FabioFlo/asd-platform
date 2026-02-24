package it.asd.competition.features.registerparticipant;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RegisterParticipantCommand(
        @NotNull UUID eventId,
        @NotNull UUID asdId,
        @NotNull UUID seasonId,
        UUID personId,    // null for team events
        UUID groupId,     // null for individual events
        String categoria,
        boolean agonistic
) {}
