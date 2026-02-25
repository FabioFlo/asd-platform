package it.asd.membership.features.addtogroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record AddToGroupCommand(
        @NotNull UUID personId,
        @NotNull UUID groupId,
        @NotNull UUID seasonId,
        @NotBlank String ruolo,
        @NotNull LocalDate dataIngresso
) {}
