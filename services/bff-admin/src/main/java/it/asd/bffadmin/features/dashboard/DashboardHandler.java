package it.asd.bffadmin.features.dashboard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dashboard handler.
 *
 * Performs a non-blocking parallel fan-out to:
 *   - membership-service  (member counts)
 *   - compliance-service  (expiry alerts)
 *   - finance-service     (revenue snapshot)
 *   - competition-service (upcoming events)
 *
 * Each call has an independent fallback: if a service is unreachable,
 * that section is null in the response and partialData is set to true.
 * The Vue SPA handles null sections gracefully.
 *
 * Responses are cached for 5 minutes to avoid hammering 4 services
 * every time a manager refreshes the dashboard.
 */
@Component
public class DashboardHandler {

    private static final Logger log = LoggerFactory.getLogger(DashboardHandler.class);

    private final WebClient membershipClient;
    private final WebClient complianceClient;
    private final WebClient financeClient;
    private final WebClient competitionClient;

    public DashboardHandler(
            @Qualifier("membershipWebClient") WebClient membershipClient,
            @Qualifier("complianceWebClient") WebClient complianceClient,
            @Qualifier("financeWebClient")    WebClient financeClient,
            @Qualifier("competitionWebClient") WebClient competitionClient) {
        this.membershipClient  = membershipClient;
        this.complianceClient  = complianceClient;
        this.financeClient     = financeClient;
        this.competitionClient = competitionClient;
    }

    @Cacheable(value = "dashboard", key = "#query.asdId() + ':' + #query.seasonId()")
    public DashboardView handle(DashboardQuery query) {
        var partial = new AtomicBoolean(false);

        // ── Parallel fan-out ─────────────────────────────────────────────
        var membershipMono  = fetchMembership(query, partial);
        var complianceMono  = fetchCompliance(query, partial);
        var financeMono     = fetchFinance(query, partial);
        var competitionMono = fetchCompetition(query, partial);

        // Zip all four — each is already defaulted to null on error
        return Mono.zip(membershipMono, complianceMono, financeMono, competitionMono)
                .map(tuple -> new DashboardView(
                        query.asdId(),
                        query.seasonId(),
                        null,   // TODO: fetch ASD name from registry-service
                        null,   // TODO: fetch season codice from registry-service
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4(),
                        partial.get()
                ))
                .block(); // BFF handlers are synchronous at the controller boundary
    }

    // ── Private fetch helpers ────────────────────────────────────────────

    private Mono<DashboardView.MembershipSummary> fetchMembership(
            DashboardQuery q, AtomicBoolean partial) {
        return membershipClient.get()
                .uri("/membership/asds/{asdId}/summary?season={seasonId}",
                        q.asdId(), q.seasonId())
                .retrieve()
                .bodyToMono(DashboardView.MembershipSummary.class)
                .doOnError(e -> {
                    log.warn("[DASHBOARD] membership-service unavailable: {}", e.getMessage());
                    partial.set(true);
                })
                .onErrorReturn(null);
    }

    private Mono<DashboardView.ComplianceSummary> fetchCompliance(
            DashboardQuery q, AtomicBoolean partial) {
        return complianceClient.get()
                .uri("/compliance/asds/{asdId}/summary?season={seasonId}",
                        q.asdId(), q.seasonId())
                .retrieve()
                .bodyToMono(DashboardView.ComplianceSummary.class)
                .doOnError(e -> {
                    log.warn("[DASHBOARD] compliance-service unavailable: {}", e.getMessage());
                    partial.set(true);
                })
                .onErrorReturn(null);
    }

    private Mono<DashboardView.FinanceSummary> fetchFinance(
            DashboardQuery q, AtomicBoolean partial) {
        return financeClient.get()
                .uri("/finance/asds/{asdId}/summary?season={seasonId}",
                        q.asdId(), q.seasonId())
                .retrieve()
                .bodyToMono(DashboardView.FinanceSummary.class)
                .doOnError(e -> {
                    log.warn("[DASHBOARD] finance-service unavailable: {}", e.getMessage());
                    partial.set(true);
                })
                .onErrorReturn(null);
    }

    private Mono<DashboardView.CompetitionSummary> fetchCompetition(
            DashboardQuery q, AtomicBoolean partial) {
        return competitionClient.get()
                .uri("/competition/asds/{asdId}/summary?season={seasonId}",
                        q.asdId(), q.seasonId())
                .retrieve()
                .bodyToMono(DashboardView.CompetitionSummary.class)
                .doOnError(e -> {
                    log.warn("[DASHBOARD] competition-service unavailable: {}", e.getMessage());
                    partial.set(true);
                })
                .onErrorReturn(null);
    }
}
