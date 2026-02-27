package it.asd.competition.features.registerparticipant;

import it.asd.common.validation.annotation.ValidUUID;

import java.util.UUID;

public record RegisterParticipantCommand(
        @ValidUUID UUID eventId,
        @ValidUUID UUID asdId,
        @ValidUUID UUID seasonId,
        UUID personId,    // null for team events
        UUID groupId,     // null for individual events
        String categoria,
        boolean agonistic
) {
}
