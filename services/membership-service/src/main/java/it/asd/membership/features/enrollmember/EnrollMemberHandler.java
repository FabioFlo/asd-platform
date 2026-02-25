package it.asd.membership.features.enrollmember;

import it.asd.common.kafka.EventPublisher;
import it.asd.events.KafkaTopics;
import it.asd.events.membership.MembershipActivatedEvent;
import it.asd.membership.shared.entity.MembershipEntity;
import it.asd.membership.shared.entity.MembershipStatus;
import it.asd.membership.shared.readmodel.PersonCacheEntity;
import it.asd.membership.shared.repository.MembershipRepository;
import it.asd.membership.shared.repository.PersonCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class EnrollMemberHandler {

    private static final Logger log = LoggerFactory.getLogger(EnrollMemberHandler.class);

    private final MembershipRepository membershipRepository;
    private final PersonCacheRepository personCacheRepository;
    private final IdentityClient identityClient;
    private final RegistryClient registryClient;
    private final EventPublisher eventPublisher;

    public EnrollMemberHandler(MembershipRepository membershipRepository,
                               PersonCacheRepository personCacheRepository,
                               IdentityClient identityClient,
                               RegistryClient registryClient,
                               EventPublisher eventPublisher) {
        this.membershipRepository = membershipRepository;
        this.personCacheRepository = personCacheRepository;
        this.identityClient = identityClient;
        this.registryClient = registryClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public EnrollMemberResult handle(EnrollMemberCommand cmd) {
        // 1. Check AlreadyEnrolled first (local DB)
        var existing = membershipRepository.findByPersonIdAndAsdIdAndSeasonId(
                cmd.personId(), cmd.asdId(), cmd.seasonId());
        if (existing.isPresent()) {
            return new EnrollMemberResult.AlreadyEnrolled(existing.get().getId());
        }

        // 2. Verify person exists via identity-service
        IdentityClient.PersonApiResponse personData;
        try {
            personData = identityClient.getPerson(cmd.personId());
        } catch (IdentityClient.IdentityCallException ex) {
            return new EnrollMemberResult.PersonNotFound(cmd.personId());
        }

        // 3. Verify active season via registry-service
        RegistryClient.SeasonApiResponse seasonData;
        try {
            seasonData = registryClient.getCurrentSeason(cmd.asdId());
        } catch (RegistryClient.RegistryCallException ex) {
            return new EnrollMemberResult.SeasonNotFound(cmd.asdId());
        }

        // 4. Generate numero tessera
        String prefix = cmd.asdId().toString().substring(0, 4).toUpperCase();
        int year = cmd.dataIscrizione().getYear();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String numeroTessera = prefix + "-" + year + "-" + suffix;

        // 5. Save membership
        var entity = MembershipEntity.builder()
                .personId(cmd.personId())
                .asdId(cmd.asdId())
                .seasonId(cmd.seasonId())
                .numeroTessera(numeroTessera)
                .dataIscrizione(cmd.dataIscrizione())
                .stato(MembershipStatus.ACTIVE)
                .note(cmd.note())
                .build();

        var saved = membershipRepository.save(entity);
        log.info("[ENROLL_MEMBER] Saved membershipId={} personId={}", saved.getId(), saved.getPersonId());

        // 6. Cache person
        var cacheOpt = personCacheRepository.findByPersonId(cmd.personId());
        if (cacheOpt.isPresent()) {
            var cache = cacheOpt.get();
            cache.setNome(personData.nome());
            cache.setCognome(personData.cognome());
            cache.setCodiceFiscale(personData.codiceFiscale());
            cache.setEmail(personData.email());
            cache.setLastSyncedAt(LocalDateTime.now());
            cache.setSource("sync_check");
            personCacheRepository.save(cache);
        } else {
            personCacheRepository.save(PersonCacheEntity.builder()
                    .personId(cmd.personId())
                    .nome(personData.nome())
                    .cognome(personData.cognome())
                    .codiceFiscale(personData.codiceFiscale())
                    .email(personData.email())
                    .lastSyncedAt(LocalDateTime.now())
                    .source("sync_check")
                    .build());
        }

        // 7. Publish event
        eventPublisher.publish(
                KafkaTopics.MEMBERSHIP_ACTIVATED,
                new MembershipActivatedEvent(
                        UUID.randomUUID(), saved.getId(), saved.getPersonId(),
                        saved.getAsdId(), saved.getSeasonId(), saved.getNumeroTessera(),
                        saved.getDataIscrizione(), Instant.now()),
                saved.getAsdId(), saved.getSeasonId());

        return new EnrollMemberResult.Enrolled(saved.getId(), saved.getNumeroTessera());
    }
}
