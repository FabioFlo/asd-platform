package it.asd.membership.features.addtogroup;

import java.util.UUID;

public sealed interface AddToGroupResult
        permits AddToGroupResult.Added,
        AddToGroupResult.GroupNotFound,
        AddToGroupResult.NotAMember,
        AddToGroupResult.AlreadyInGroup {

    record Added(UUID enrollmentId) implements AddToGroupResult {
    }

    record GroupNotFound(UUID groupId) implements AddToGroupResult {
    }

    record NotAMember(String detail) implements AddToGroupResult {
    }

    record AlreadyInGroup(UUID existingId) implements AddToGroupResult {
    }
}
