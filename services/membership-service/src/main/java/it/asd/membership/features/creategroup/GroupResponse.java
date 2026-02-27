package it.asd.membership.features.creategroup;

import java.util.UUID;

public record GroupResponse(UUID groupId, UUID asdId, UUID seasonId, String nome) {
}
