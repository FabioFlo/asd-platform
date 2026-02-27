package it.asd.identity.features.updateperson;

import java.util.UUID;

public sealed interface UpdatePersonResult
        permits UpdatePersonResult.Updated,
        UpdatePersonResult.NotFound,
        UpdatePersonResult.DuplicateEmail {

    record Updated(UUID personId) implements UpdatePersonResult {
    }

    record NotFound(UUID personId) implements UpdatePersonResult {
    }

    record DuplicateEmail(String email) implements UpdatePersonResult {
    }
}
