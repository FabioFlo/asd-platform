package it.asd.bffadmin.features.dashboard;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Aggregated dashboard view for the ASD manager.
 *
 * Each section is Optional-style: null means the downstream service
 * was unavailable (partial_data). The Vue SPA checks for null and
 * shows a "temporarily unavailable" notice per section.
 */
public record DashboardView(
        UUID asdId,
        UUID seasonId,
        String asdName,
        String seasonCodice,

        // ── Membership counters (from membership-service) ──────────────
        MembershipSummary membership,

        // ── Compliance alerts (from compliance-service) ────────────────
        ComplianceSummary compliance,

        // ── Finance snapshot (from finance-service) ────────────────────
        FinanceSummary finance,

        // ── Upcoming events (from competition-service) ─────────────────
        CompetitionSummary competition,

        // ── Meta ────────────────────────────────────────────────────────
        boolean partialData  // true if any downstream call failed
) {

    public record MembershipSummary(
            int activeMembers,
            int suspendedMembers,
            int pendingActivation
    ) {}

    public record ComplianceSummary(
            int expiringIn30Days,      // docs expiring within 30 days
            int expired,               // already expired / blocking
            int missingForNewMembers   // enrolled but docs never uploaded
    ) {}

    public record FinanceSummary(
            BigDecimal revenueThisSeason,
            BigDecimal pendingAmount,
            int overduePayments,
            int receiptsIssued
    ) {}

    public record CompetitionSummary(
            int upcomingEvents,
            int ongoingEvents,
            int completedEvents
    ) {}
}
