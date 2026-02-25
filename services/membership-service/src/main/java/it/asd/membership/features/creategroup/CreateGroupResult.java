package it.asd.membership.features.creategroup;

import java.util.UUID;

public sealed interface CreateGroupResult
        permits CreateGroupResult.Created, CreateGroupResult.DuplicateName {

    record Created(UUID groupId) implements CreateGroupResult {}
    record DuplicateName(String nome) implements CreateGroupResult {}
}
