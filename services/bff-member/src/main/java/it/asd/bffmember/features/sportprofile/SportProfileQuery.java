package it.asd.bffmember.features.sportprofile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SportProfileQuery(
        @NotNull  UUID   personId,
        @NotBlank String disciplina
) {}
