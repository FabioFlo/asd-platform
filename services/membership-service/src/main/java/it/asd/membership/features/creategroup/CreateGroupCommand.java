package it.asd.membership.features.creategroup;

import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CreateGroupCommand(
        @ValidUUID UUID asdId,
        @ValidUUID UUID seasonId,
        @NotBlank String nome,
        @NotBlank String disciplina,
        @NotBlank String tipo,
        String note
) {
}
