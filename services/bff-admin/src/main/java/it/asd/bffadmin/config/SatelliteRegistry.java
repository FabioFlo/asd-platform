package it.asd.bffadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Reads the `satellites` list from application.yml.
 *
 * Each entry describes one sport satellite service.
 * The BFF creates one WebClient per entry at startup.
 * Adding a new sport = adding one block to application.yml.
 * No Java changes required.
 */
@ConfigurationProperties(prefix = "")
public record SatelliteRegistry(List<SatelliteDefinition> satellites) {

    public record SatelliteDefinition(
            String name,          // "chess" | "padel" | "football"
            String baseUrl,       // "http://chess-service:8101"
            String disciplina,    // "scacchi" | "padel" | "calcio"
            String displayLabel,  // "Chess" | "Padel" | "Football"
            String badgeColor,    // hex colour for the Passport chip
            String profileRoute   // Vue router path: "/chess" | "/padel" | "/football"
    ) {}

    /**
     * Find the definition for a given disciplina string.
     * Returns null if no satellite is registered for that disciplina.
     */
    public SatelliteDefinition findByDisciplina(String disciplina) {
        return satellites.stream()
                .filter(s -> s.disciplina().equals(disciplina))
                .findFirst()
                .orElse(null);
    }
}
