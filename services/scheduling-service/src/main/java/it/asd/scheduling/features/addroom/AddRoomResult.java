package it.asd.scheduling.features.addroom;

import java.util.UUID;

public sealed interface AddRoomResult
        permits AddRoomResult.Added, AddRoomResult.VenueNotFound, AddRoomResult.DuplicateName {

    record Added(UUID roomId) implements AddRoomResult {}
    record VenueNotFound(UUID venueId) implements AddRoomResult {}
    record DuplicateName(String nome) implements AddRoomResult {}
}
