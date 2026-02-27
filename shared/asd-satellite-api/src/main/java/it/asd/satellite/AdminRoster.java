package it.asd.satellite;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Roster view for the admin sport roster endpoint.
 *
 * Returned by: GET /satellite/roster?asd={asdId}&season={seasonId}
 *
 * Each entry is one player with their current rank/stats.
 * `stats` is an open Map — satellite populates it freely.
 */
public record AdminRoster(
        String disciplina,
        List<RosterEntry> entries
) {
    public record RosterEntry(
            UUID personId,
            String nome,
            String cognome,
            String rankLabel,
            Map<String, Object> stats
    ) {}
}
