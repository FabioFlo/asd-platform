package it.asd.competition.features.registerparticipant;

import java.util.List;
import java.util.UUID;

/**
 * Sealed result with four distinct outcomes.
 *
 * The compiler forces every caller (controller, tests) to handle ALL cases.
 * No hidden exceptions, no boolean flags, no null checks.
 *
 * ComplianceUnavailable is the FAIL-CLOSED case:
 *   when Compliance is unreachable, we return this instead of guessing.
 *   The controller maps it to HTTP 503 with a Retry-After header.
 */
public sealed interface RegisterParticipantResult
        permits RegisterParticipantResult.Registered,
                RegisterParticipantResult.Ineligible,
                RegisterParticipantResult.AlreadyRegistered,
                RegisterParticipantResult.ComplianceUnavailable {

    /** Happy path: participation was created and the event was published. */
    record Registered(UUID participationId) implements RegisterParticipantResult {}

    /**
     * FAIL-CLOSED: person is ineligible to compete.
     * blockingDocuments explains exactly which documents are missing/expired.
     * This case covers BOTH cached ineligibility AND fresh sync check failure.
     */
    record Ineligible(List<String> blockingDocuments) implements RegisterParticipantResult {}

    /** Person/group is already registered for this event. */
    record AlreadyRegistered(UUID existingParticipationId) implements RegisterParticipantResult {}

    /**
     * FAIL-CLOSED: the Compliance service was unreachable AND there is no cached
     * eligibility entry for this person. We cannot verify → we deny registration.
     *
     * The controller returns 503 with Retry-After so the client knows to retry.
     * This is intentionally not merged with Ineligible: the cause is different
     * (infra failure vs. genuine ineligibility) and the client UI should differ.
     */
    record ComplianceUnavailable(String reason) implements RegisterParticipantResult {}
}
