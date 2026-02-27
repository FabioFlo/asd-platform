package it.asd.membership.features.enrollmember;

import java.util.UUID;

public sealed interface EnrollMemberResult
        permits EnrollMemberResult.Enrolled,
        EnrollMemberResult.PersonNotFound,
        EnrollMemberResult.AsdNotFound,
        EnrollMemberResult.SeasonNotFound,
        EnrollMemberResult.AlreadyEnrolled {

    record Enrolled(UUID membershipId, String numeroTessera) implements EnrollMemberResult {
    }

    record PersonNotFound(UUID personId) implements EnrollMemberResult {
    }

    record AsdNotFound(UUID asdId) implements EnrollMemberResult {
    }

    record SeasonNotFound(UUID asdId) implements EnrollMemberResult {
    }

    record AlreadyEnrolled(UUID existingId) implements EnrollMemberResult {
    }
}
