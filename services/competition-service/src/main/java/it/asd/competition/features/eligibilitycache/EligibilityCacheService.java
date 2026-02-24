package it.asd.competition.features.eligibilitycache;

import it.asd.competition.shared.entity.EligibilityCacheEntity;
import it.asd.competition.shared.repository.EligibilityCacheRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Manages the local eligibility cache.
 * Updated from two sources:
 *   1. Sync check result (RegisterParticipantHandler cold path)
 *   2. Async Kafka events (ComplianceEventConsumer)
 */
@Component
public class EligibilityCacheService {

    private final EligibilityCacheRepository repo;

    public EligibilityCacheService(EligibilityCacheRepository repo) {
        this.repo = repo;
    }

    /**
     * Wraps the raw entity in a typed record so callers
     * don't depend on the JPA entity directly.
     */
    public record CachedEligibility(boolean isEligible, List<String> blockingDocuments) {}

    @Transactional(readOnly = true)
    public Optional<CachedEligibility> get(UUID personId, UUID asdId) {
        return repo.findByPersonIdAndAsdId(personId, asdId)
                .map(e -> new CachedEligibility(e.isEligible(), e.getBlockingDocuments()));
    }

    @Transactional
    public void updateFromSyncCheck(UUID personId, UUID asdId,
                                    boolean eligible, List<String> blockingDocs) {
        upsert(personId, asdId, eligible, blockingDocs, "sync_check");
    }

    @Transactional
    public void markIneligible(UUID personId, UUID asdId,
                               String documentType, String reason) {
        var cached = repo.findByPersonIdAndAsdId(personId, asdId)
                .orElseGet(() -> newEntry(personId, asdId));

        var blocking = new java.util.ArrayList<>(cached.getBlockingDocuments());
        var entry = documentType + " [" + reason + "]";
        if (!blocking.contains(entry)) blocking.add(entry);

        cached.setEligible(false);
        cached.setBlockingDocuments(blocking);
        cached.setSource("document.expired");
        repo.save(cached);
    }

    @Transactional
    public void removeBlocker(UUID personId, UUID asdId, String documentType) {
        repo.findByPersonIdAndAsdId(personId, asdId).ifPresent(cached -> {
            var remaining = cached.getBlockingDocuments().stream()
                    .filter(d -> !d.startsWith(documentType))
                    .toList();
            cached.setBlockingDocuments(remaining);
            cached.setEligible(remaining.isEmpty());
            cached.setSource("document.renewed");
            repo.save(cached);
        });
    }

    private void upsert(UUID personId, UUID asdId,
                        boolean eligible, List<String> blocking, String source) {
        var entity = repo.findByPersonIdAndAsdId(personId, asdId)
                .orElseGet(() -> newEntry(personId, asdId));
        entity.setEligible(eligible);
        entity.setBlockingDocuments(new java.util.ArrayList<>(blocking));
        entity.setSource(source);
        repo.save(entity);
    }

    private EligibilityCacheEntity newEntry(UUID personId, UUID asdId) {
        return EligibilityCacheEntity.builder()
                .personId(personId)
                .asdId(asdId)
                .eligible(false)
                .source("unknown")
                .build();
    }
}
