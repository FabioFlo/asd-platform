package it.asd.scheduling.features.createvenue;

import java.util.UUID;

public sealed interface CreateVenueResult
        permits CreateVenueResult.Created, CreateVenueResult.DuplicateName {

    record Created(UUID venueId) implements CreateVenueResult {}
    record DuplicateName(String nome) implements CreateVenueResult {}
}
