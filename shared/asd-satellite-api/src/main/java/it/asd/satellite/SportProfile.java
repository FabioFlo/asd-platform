package it.asd.satellite;

import java.util.Map;
import java.util.UUID;

/**
 * Full sport profile for the member's sport-specific detail page.
 *
 * Returned by: GET /satellite/players/{personId}/profile
 *
 * `data` is an open Map — each satellite puts whatever it needs here.
 * The Vue SPA receives this and the sport-specific component knows
 * how to render it. The BFF is a transparent passthrough for `data`.
 *
 * Examples:
 *   chess:    data = { "currentElo": 1450, "games": [...], "fideId": "..." }
 *   padel:    data = { "categoria": "2°", "punti": 340, "matches": [...] }
 *   football: data = { "ruolo": "Attaccante", "goals": 12, "lineups": [...] }
 */
public record SportProfile(
        UUID personId,
        SportSummary summary,
        Map<String, Object> data
) {}
