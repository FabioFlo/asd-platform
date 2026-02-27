package it.asd.membership.features.addtogroup;

import it.asd.common.enums.AsdRole;
import it.asd.common.validation.annotation.ValidRole;
import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AddToGroupCommand(
        @ValidUUID UUID personId,
        @ValidUUID UUID groupId,
        @ValidUUID UUID seasonId,
        @ValidRole AsdRole ruolo,
        @NotNull LocalDate dataIngresso
) {
}
