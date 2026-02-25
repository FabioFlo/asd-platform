package it.asd.membership.features.creategroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateGroupCommand(
        @NotNull UUID asdId,
        @NotNull UUID seasonId,
        @NotBlank String nome,
        @NotBlank String disciplina,
        @NotBlank String tipo,
        String note
) {}
