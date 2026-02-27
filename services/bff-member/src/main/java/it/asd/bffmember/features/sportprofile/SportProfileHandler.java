package it.asd.bffmember.features.sportprofile;

import it.asd.bffmember.config.SatelliteRegistry;
import it.asd.satellite.SportProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Fetches the full sport profile for a person from the relevant satellite.
 *
 * Sport-agnostic: looks up the satellite by disciplina from the registry.
 * If no satellite is registered, returns 404.
 * If the satellite is unreachable, returns 503.
 */
@Component
public class SportProfileHandler {

    private static final Logger log = LoggerFactory.getLogger(SportProfileHandler.class);

    private final Map<String, WebClient> satelliteClients;
    private final SatelliteRegistry      registry;

    public SportProfileHandler(
            Map<String, WebClient> satelliteClients,
            SatelliteRegistry      registry) {
        this.satelliteClients = satelliteClients;
        this.registry         = registry;
    }

    public SportProfile handle(SportProfileQuery query) {
        var def    = registry.findByDisciplina(query.disciplina());
        var client = satelliteClients.get(query.disciplina());

        if (def == null || client == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No satellite registered for disciplina: " + query.disciplina());
        }

        return client.get()
                .uri("/satellite/players/{id}/profile", query.personId())
                .retrieve()
                .bodyToMono(SportProfile.class)
                .doOnError(e -> log.warn("[SPORT_PROFILE] satellite '{}' unavailable: {}",
                        def.name(), e.getMessage()))
                .onErrorMap(e -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Satellite '" + def.name() + "' is temporarily unavailable"))
                .block();
    }
}
