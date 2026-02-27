package it.asd.bffmember.features.passport;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * The Sports Passport — hero view for the Member Frontoffice.
 *
 * One PassportView per authenticated person. Contains one AsdCard per ASD
 * the person belongs to, each card carrying compliance summary and a list
 * of SportChip entries (one per discipline the person is active in).
 *
 * The Vue SPA renders this as a card grid. Clicking a SportChip
 * navigates to the sport-specific profile route (/chess, /padel, /football).
 *
 * partialData = true means one or more satellites were unreachable.
 * Null sport chips are omitted from the list (not shown in the SPA).
 */
public record PassportView(
        UUID personId,
        String nome,
        String cognome,
        List<AsdCard> asds,
        WalletSummary wallet,
        boolean partialData
) {

    /**
     * One card per ASD the member belongs to.
     */
    public record AsdCard(
            UUID asdId,
            String asdName,
            String logoUrl,          // nullable — served via media service
            String membershipStatus, // ACTIVE | SUSPENDED | PENDING
            String numeroTessera,
            ComplianceBadge compliance,
            List<SportChip> sports
    ) {}

    /**
     * Compact compliance summary for the card badge.
     * status drives CSS: OK=green, EXPIRING_SOON=amber, BLOCKED=red
     */
    public record ComplianceBadge(
            String status,           // OK | EXPIRING_SOON | BLOCKED
            LocalDate nextExpiry,    // nearest expiring document date
            int blockingCount        // number of documents blocking participation
    ) {}

    /**
     * One chip per sport discipline the member is active in within this ASD.
     * Clicking navigates to the sport-specific profile page.
     */
    public record SportChip(
            String disciplina,       // "scacchi" | "padel" | "calcio"
            String displayLabel,     // "Chess" | "Padel" | "Football"
            String rankLabel,        // e.g. "ELO 1450" | "2° Categoria" | "Attaccante"
            String badgeColor,       // hex colour code for the chip
            String profileRoute      // Vue router path: "/chess" | "/padel" | "/football"
    ) {}

    /**
     * Financial summary across all ASDs (read from finance-service).
     */
    public record WalletSummary(
            BigDecimal totalPending,
            int overdueCount,
            LocalDate nextPaymentDue // nearest pending due date
    ) {}
}
