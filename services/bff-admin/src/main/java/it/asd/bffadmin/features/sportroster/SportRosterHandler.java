package it.asd.bffadmin.features.sportroster;

import it.asd.bffadmin.config.SatelliteRegistry;
import it.asd.satellite.AdminRoster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

/**
 * Fetches the admin roster for a sport from the relevant satellite.
 * Sport-agnostic: routes by disciplina to the correct satellite.
 */
@Component
public class SportRosterHandler {

    private static final Logger log = LoggerFactory.getLogger(SportRosterHandler.class);

    private final Map<String, WebClient> satelliteClients;
    private final SatelliteRegistry      registry;

    public SportRosterHandler(
            Map<String, WebClient> satelliteClients,
            SatelliteRegistry      registry) {
        this.satelliteClients = satelliteClients;
        this.registry         = registry;
    }

    public AdminRoster handle(UUID asdId, UUID seasonId, String disciplina) {
        var def    = registry.findByDisciplina(disciplina);
        var client = satelliteClients.get(disciplina);

        if (def == null || client == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No satellite registered for disciplina: " + disciplina);
        }

        return client.get()
                .uri(u -> u.path("/satellite/roster")
                        .queryParam("asd", asdId)
                        .queryParam("season", seasonId)
                        .build())
                .retrieve()
                .bodyToMono(AdminRoster.class)
                .doOnError(e -> log.warn("[SPORT_ROSTER] satellite '{}' unavailable: {}",
                        def.name(), e.getMessage()))
                .onErrorMap(e -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "Satellite '" + def.name() + "' is temporarily unavailable"))
                .block();
    }
}
