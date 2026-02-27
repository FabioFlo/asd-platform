package it.asd.membership.features.enrollmember;

import java.util.UUID;

public record MembershipResponse(UUID membershipId, String numeroTessera) {
}
