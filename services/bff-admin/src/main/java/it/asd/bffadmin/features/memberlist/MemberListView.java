package it.asd.bffadmin.features.memberlist;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Paginated member list with compliance badge per row.
 * Aggregates membership-service (list) + compliance-service (status per person).
 */
public record MemberListView(
        List<MemberRow> members,
        int totalElements,
        int totalPages,
        int currentPage
) {

    /**
     * One row in the member list table.
     * complianceStatus drives the badge colour in the Vue SPA:
     *   OK → green, EXPIRING_SOON → amber, BLOCKED → red, UNKNOWN → grey
     */
    public record MemberRow(
            UUID personId,
            String nome,
            String cognome,
            String codiceFiscale,
            String numeroTessera,
            String membershipStatus,    // ACTIVE | SUSPENDED | PENDING
            String complianceStatus,    // OK | EXPIRING_SOON | BLOCKED | UNKNOWN
            LocalDate nextDocumentExpiry,
            List<String> activeRoles
    ) {}
}
