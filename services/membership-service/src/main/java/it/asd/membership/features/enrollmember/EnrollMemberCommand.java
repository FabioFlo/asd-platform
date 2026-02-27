package it.asd.membership.features.enrollmember;

import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record EnrollMemberCommand(
        @ValidUUID UUID personId,
        @ValidUUID UUID asdId,
        @ValidUUID UUID seasonId,
        @NotNull LocalDate dataIscrizione,
        String note
) {
}
