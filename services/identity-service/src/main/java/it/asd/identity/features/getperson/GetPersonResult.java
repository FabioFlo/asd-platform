package it.asd.identity.features.getperson;

import it.asd.identity.features.registerperson.PersonResponse;

import java.util.UUID;

public sealed interface GetPersonResult
        permits GetPersonResult.Found, GetPersonResult.NotFound {

    record Found(PersonResponse response) implements GetPersonResult {
    }

    record NotFound(UUID personId) implements GetPersonResult {
    }
}
