package it.asd.identity.features.registerperson;

import it.asd.identity.shared.entity.PersonEntity;

import java.time.LocalDate;
import java.util.UUID;

public record PersonResponse(
        UUID id,
        String codiceFiscale,
        String nome,
        String cognome,
        LocalDate dataNascita,
        String email,
        String stato
) {
    public static PersonResponse from(PersonEntity e) {
        return new PersonResponse(e.getId(), e.getCodiceFiscale(), e.getNome(),
                e.getCognome(), e.getDataNascita(), e.getEmail(), e.getStato().name());
    }
}
