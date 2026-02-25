package it.asd.identity.features.updateperson;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdatePersonCommand(
        @NotNull UUID personId,
        String nome,
        String cognome,
        String email,
        String telefono,
        String indirizzo,
        String citta,
        String provincia,
        String cap
) {}
