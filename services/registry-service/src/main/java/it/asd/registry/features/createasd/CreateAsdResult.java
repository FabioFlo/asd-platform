package it.asd.registry.features.createasd;

import java.util.UUID;

public sealed interface CreateAsdResult
        permits CreateAsdResult.Created, CreateAsdResult.DuplicateCodiceFiscale {

    record Created(UUID asdId, String nome) implements CreateAsdResult {}
    record DuplicateCodiceFiscale(String cf) implements CreateAsdResult {}
}
