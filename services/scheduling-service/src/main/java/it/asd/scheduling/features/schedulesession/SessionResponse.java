package it.asd.scheduling.features.schedulesession;

import java.util.UUID;

public record SessionResponse(UUID sessionId, UUID asdId, UUID venueId, String titolo) {}
