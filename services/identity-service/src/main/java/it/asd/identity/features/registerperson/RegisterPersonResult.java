package it.asd.identity.features.registerperson;

import java.util.UUID;

public sealed interface RegisterPersonResult
        permits RegisterPersonResult.Registered,
        RegisterPersonResult.DuplicateCodiceFiscale,
        RegisterPersonResult.DuplicateEmail {

    record Registered(UUID personId) implements RegisterPersonResult {
    }

    record DuplicateCodiceFiscale(String cf) implements RegisterPersonResult {
    }

    record DuplicateEmail(String email) implements RegisterPersonResult {
    }
}
