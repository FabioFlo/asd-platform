package it.asd.registry.features.createasd;

import jakarta.validation.constraints.NotBlank;

public record CreateAsdCommand(
        @NotBlank String codiceFiscale,
        @NotBlank String nome,
        String codiceAffiliazioneConi,
        String codiceAffiliazioneFsn,
        String disciplina,
        String citta,
        String provincia,
        String email,
        String telefono
) {
}
