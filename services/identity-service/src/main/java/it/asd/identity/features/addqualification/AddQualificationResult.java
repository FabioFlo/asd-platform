package it.asd.identity.features.addqualification;

import java.util.UUID;

public sealed interface AddQualificationResult
        permits AddQualificationResult.Added, AddQualificationResult.PersonNotFound {

    record Added(UUID qualificationId) implements AddQualificationResult {}
    record PersonNotFound(UUID personId) implements AddQualificationResult {}
}
