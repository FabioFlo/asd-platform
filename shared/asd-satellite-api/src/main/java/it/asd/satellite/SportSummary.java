package it.asd.satellite;

import java.time.Instant;
import java.util.UUID;

/**
 * Minimal sport summary for the Sports Passport chip.
 *
 * Returned by: GET /satellite/players/{personId}/summary
 *
 * Every satellite MUST implement this endpoint and return this exact shape.
 * The BFF does not need to know what sport it is — it renders the rankLabel
 * and routes to profileRoute.
 */
public record SportSummary(
        UUID personId,
        String disciplina,       // "scacchi" | "padel" | "calcio" | future sports
        String rankLabel,        // "ELO 1450" | "2° Categoria" | "Attaccante"
        boolean eligible,        // true = can participate (quick flag for badge colour)
        Instant lastActivity     // nullable — null if person has never competed
) {}
