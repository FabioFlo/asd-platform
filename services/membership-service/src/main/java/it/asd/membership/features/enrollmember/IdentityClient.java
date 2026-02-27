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
class IdentityClient {

    private static final Logger log = LoggerFactory.getLogger(IdentityClient.class);

    private final RestClient restClient;

    IdentityClient(@Value("${services.identity.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    PersonApiResponse getPerson(UUID personId) {
        try {
            var response = restClient.get()
                    .uri("/identity/persons/{id}", personId)
                    .retrieve()
                    .body(PersonApiResponse.class);
            if (response == null) throw new IdentityCallException("Null response from Identity");
            return response;
        } catch (RestClientException ex) {
            log.error("[IDENTITY-CLIENT] Unreachable for personId={}: {}", personId, ex.getMessage());
            throw new IdentityCallException("Identity unreachable: " + ex.getMessage());
        }
    }

    record PersonApiResponse(UUID id, String codiceFiscale, String nome, String cognome,
                             LocalDate dataNascita, String email, String stato) {
    }

    static final class IdentityCallException extends RuntimeException {
        IdentityCallException(String msg) {
            super(msg);
        }
    }
}
