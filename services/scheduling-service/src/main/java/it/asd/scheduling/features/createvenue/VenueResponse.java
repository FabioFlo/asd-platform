package it.asd.scheduling.features.createvenue;

import java.util.UUID;

public record VenueResponse(UUID venueId, UUID asdId, String nome) {
}
