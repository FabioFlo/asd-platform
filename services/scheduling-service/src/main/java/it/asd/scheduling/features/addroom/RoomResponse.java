package it.asd.scheduling.features.addroom;

import java.util.UUID;

public record RoomResponse(UUID roomId, UUID venueId, String nome) {}
