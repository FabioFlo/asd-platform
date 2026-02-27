package it.asd.bffmember.features.passport;

import it.asd.bffmember.config.SatelliteRegistry;
import it.asd.satellite.SportSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Passport handler — assembles the Sports Passport for one person.
 *
 * Sport-agnostic: iterates SatelliteRegistry to discover all registered
 * sports. Adding a new sport never requires changes here.
 *
 * Aggregation:
 * 1. GET /membership/persons/{personId}/memberships        → list of ASDs
 * 2. For each ASD in parallel:
 *    a. GET /compliance/persons/{personId}/status?asd=     → ComplianceBadge
 *    b. GET /membership/persons/{personId}/disciplines?asd= → List<String>
 * 3. For each disciplina, look up the satellite from registry and call:
 *    GET /satellite/players/{personId}/summary             → SportSummary
 * 4. GET /finance/persons/{personId}/wallet                → WalletSummary
 * 5. Zip into PassportView.
 */
@Component
public class PassportHandler {

    private static final Logger log = LoggerFactory.getLogger(PassportHandler.class);

    private final WebClient membershipClient;
    private final WebClient complianceClient;
    private final WebClient financeClient;
    private final Map<String, WebClient> satelliteClients;   // keyed by disciplina
    private final SatelliteRegistry registry;

    public PassportHandler(
            @Qualifier("membershipWebClient") WebClient membershipClient,
            @Qualifier("complianceWebClient") WebClient complianceClient,
            @Qualifier("financeWebClient")    WebClient financeClient,
            Map<String, WebClient>            satelliteClients,
            SatelliteRegistry                 registry) {
        this.membershipClient  = membershipClient;
        this.complianceClient  = complianceClient;
        this.financeClient     = financeClient;
        this.satelliteClients  = satelliteClients;
        this.registry          = registry;
    }

    @Cacheable(value = "passport", key = "#personId")
    public PassportView handle(UUID personId) {
        var partial = new AtomicBoolean(false);

        // TODO Step 1: GET /membership/persons/{personId}/memberships
        // TODO Step 2: per-ASD compliance + discipline fan-out
        // TODO Step 3: per-disciplina satellite summary (see fetchSportChip below)
        // TODO Step 4: GET /finance/persons/{personId}/wallet
        // TODO Step 5: zip into PassportView

        throw new UnsupportedOperationException("TODO: implement PassportHandler");
    }

    // ── Sport chip fetch — sport-agnostic ─────────────────────────────────

    /**
     * Fetches a SportChip for one disciplina.
     *
     * Looks up the satellite WebClient from the registry by disciplina.
     * If no satellite is registered for this disciplina, logs a warning
     * and returns Mono.empty() (chip is omitted from the Passport).
     * If the satellite is unreachable, same: empty + partialData = true.
     */
    private Mono<PassportView.SportChip> fetchSportChip(
            UUID personId, String disciplina, AtomicBoolean partial) {

        var def    = registry.findByDisciplina(disciplina);
        var client = satelliteClients.get(disciplina);

        if (def == null || client == null) {
            log.warn("[PASSPORT] No satellite registered for disciplina '{}' — skipping chip",
                    disciplina);
            return Mono.empty();
        }

        return client.get()
                .uri("/satellite/players/{id}/summary", personId)
                .retrieve()
                .bodyToMono(SportSummary.class)
                .map(summary -> new PassportView.SportChip(
                        summary.disciplina(),
                        def.displayLabel(),
                        summary.rankLabel(),
                        def.badgeColor(),
                        def.profileRoute()
                ))
                .doOnError(e -> {
                    log.warn("[PASSPORT] satellite '{}' unavailable for person {}: {}",
                            def.name(), personId, e.getMessage());
                    partial.set(true);
                })
                .onErrorResume(e -> Mono.empty());
    }
}
