package it.asd.competition.features.registerparticipant;

import it.asd.common.kafka.EventPublisher;
import it.asd.competition.features.eligibilitycache.EligibilityCacheService;
import it.asd.competition.shared.entity.EventParticipationEntity;
import it.asd.competition.shared.entity.ParticipationStatus;
import it.asd.competition.shared.repository.EventParticipationRepository;
import it.asd.events.KafkaTopics;
import it.asd.events.competition.ParticipantRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Vertical slice handler: owns ALL logic for registering a participant.
 * <p>
 * Eligibility resolution order (fail-closed at every step):
 * 1. Check local eligibility cache (warm path — no network call)
 * 2. If cache says INELIGIBLE → deny immediately
 * 3. If cache is absent → call Compliance sync (cold path)
 * 4. If Compliance unreachable → DENY (fail-closed, return ComplianceUnavailable)
 * 5. If Compliance says ineligible → DENY, update cache
 * 6. If eligible → persist, publish event, update cache
 */
@Component
public class RegisterParticipantHandler {

    private static final Logger log = LoggerFactory.getLogger(RegisterParticipantHandler.class);

    private final EventParticipationRepository participationRepo;
    private final EligibilityCacheService eligibilityCache;
    private final ComplianceClient complianceClient;
    private final EventPublisher eventPublisher;

    public RegisterParticipantHandler(
            EventParticipationRepository participationRepo,
            EligibilityCacheService eligibilityCache,
            ComplianceClient complianceClient,
            EventPublisher eventPublisher) {
        this.participationRepo = participationRepo;
        this.eligibilityCache = eligibilityCache;
        this.complianceClient = complianceClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RegisterParticipantResult handle(RegisterParticipantCommand cmd) {

        // ── Step 1: duplicate check ───────────────────────────────────────────
        if (cmd.personId() != null) {
            var existing = participationRepo.findByPersonIdAndAsdId(cmd.personId(), cmd.asdId())
                    .stream()
                    .filter(p -> p.getEventId().equals(cmd.eventId()))
                    .findFirst();
            if (existing.isPresent())
                return new RegisterParticipantResult.AlreadyRegistered(existing.get().getId());
        }

        // ── Step 2+3+4: eligibility check (cache → sync → fail-closed) ────────
        if (cmd.personId() != null) {
            var eligibility = resolveEligibility(cmd);
            if (eligibility instanceof RegisterParticipantResult r) return r;
            // null means "eligible — proceed"
        }

        // ── Step 5: persist ───────────────────────────────────────────────────
        var participation = EventParticipationEntity.builder()
                .eventId(cmd.eventId())
                .personId(cmd.personId())
                .groupId(cmd.groupId())
                .asdId(cmd.asdId())
                .seasonId(cmd.seasonId())
                .categoria(cmd.categoria())
                .stato(ParticipationStatus.REGISTERED)
                .build();
        var saved = participationRepo.save(participation);

        // ── Step 6: publish domain event ─────────────────────────────────────
        eventPublisher.publish(
                KafkaTopics.PARTICIPANT_REGISTERED,
                new ParticipantRegisteredEvent(
                        UUID.randomUUID(), saved.getId(), saved.getEventId(),
                        saved.getPersonId(), saved.getGroupId(),
                        saved.getAsdId(), saved.getSeasonId(),
                        saved.getCategoria(), Instant.now()),
                saved.getAsdId(), saved.getSeasonId());

        log.info("[REGISTER] participationId={} eventId={} personId={}",
                saved.getId(), saved.getEventId(), saved.getPersonId());

        return new RegisterParticipantResult.Registered(saved.getId());
    }

    /**
     * Returns a non-null RegisterParticipantResult if eligibility blocks registration,
     * or null if the person is eligible and we should proceed.
     * <p>
     * This helper keeps the main handle() method readable.
     */
    private RegisterParticipantResult resolveEligibility(RegisterParticipantCommand cmd) {
        // Warm path: local cache
        var cached = eligibilityCache.get(cmd.personId(), cmd.asdId());

        if (cached.isPresent()) {
            var entry = cached.get();
            if (!entry.isEligible())
                return new RegisterParticipantResult.Ineligible(entry.blockingDocuments());
            return null; // cached eligible → proceed
        }

        // Cold path: sync call to Compliance
        try {
            var response = complianceClient.checkEligibility(
                    cmd.personId(), cmd.asdId(), cmd.agonistic());

            // Update cache from sync result
            eligibilityCache.updateFromSyncCheck(
                    cmd.personId(), cmd.asdId(),
                    response.eligible(), response.blockingDocuments());

            if (!response.eligible())
                return new RegisterParticipantResult.Ineligible(response.blockingDocuments());

            return null; // eligible → proceed

        } catch (ComplianceClient.ComplianceCallException ex) {
            // FAIL-CLOSED: no cache + can't reach Compliance → deny
            log.warn("[REGISTER] FAIL-CLOSED: Compliance unreachable for personId={}", cmd.personId());
            return new RegisterParticipantResult.ComplianceUnavailable(ex.getMessage());
        }
    }
}
