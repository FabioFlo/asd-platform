package it.asd.identity.features.registerperson;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record RegisterPersonCommand(
        @NotBlank String codiceFiscale,
        @NotBlank String nome,
        @NotBlank String cognome,
        LocalDate dataNascita,
        String luogoNascita,
        char codiceProvinciaNascita,
        String email,
        String telefono,
        String indirizzo,
        String citta,
        String provincia,
        String cap
) {}
