package it.asd.membership.features.enrollmember;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.UUID;

@Component
class RegistryClient {

    private static final Logger log = LoggerFactory.getLogger(RegistryClient.class);

    private final RestClient restClient;

    RegistryClient(@Value("${services.registry.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    SeasonApiResponse getCurrentSeason(UUID asdId) {
        try {
            var response = restClient.get()
                    .uri("/registry/asd/{id}/season/current", asdId)
                    .retrieve()
                    .body(SeasonApiResponse.class);
            if (response == null) throw new RegistryCallException("Null response from Registry");
            return response;
        } catch (RestClientException ex) {
            log.error("[REGISTRY-CLIENT] Unreachable for asdId={}: {}", asdId, ex.getMessage());
            throw new RegistryCallException("Registry unreachable: " + ex.getMessage());
        }
    }

    record SeasonApiResponse(UUID seasonId, UUID asdId, String codice,
                             LocalDate dataInizio, LocalDate dataFine) {}

    static final class RegistryCallException extends RuntimeException {
        RegistryCallException(String msg) { super(msg); }
    }
}
