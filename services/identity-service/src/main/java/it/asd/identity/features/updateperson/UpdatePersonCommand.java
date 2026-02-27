package it.asd.identity.features.updateperson;

import it.asd.common.validation.annotation.ValidUUID;

import java.util.UUID;

public record UpdatePersonCommand(
        @ValidUUID UUID personId,
        String nome,
        String cognome,
        String email,
        String telefono,
        String indirizzo,
        String citta,
        String provincia,
        String cap
) {
}
