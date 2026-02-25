package it.asd.membership.features.enrollmember;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record EnrollMemberCommand(
        @NotNull UUID personId,
        @NotNull UUID asdId,
        @NotNull UUID seasonId,
        @NotNull LocalDate dataIscrizione,
        String note
) {}
