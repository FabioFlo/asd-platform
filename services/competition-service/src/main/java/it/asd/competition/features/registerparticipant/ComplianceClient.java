package it.asd.competition.features.registerparticipant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;

/**
 * Thin HTTP client for the Compliance service eligibility endpoint.
 *
 * FAIL-CLOSED CONTRACT:
 * Any network/timeout/5xx error throws ComplianceCallException.
 * The caller (RegisterParticipantHandler) catches this and returns
 * RegisterParticipantResult.ComplianceUnavailable — never silently allows through.
 *
 * Kept inside the feature package: only RegisterParticipantHandler uses it.
 * If another feature needed compliance calls, it would get its own client or
 * this would be promoted to shared/.
 */
@Component
class ComplianceClient {

    private static final Logger log = LoggerFactory.getLogger(ComplianceClient.class);

    private final RestClient restClient;

    ComplianceClient(@Value("${services.compliance.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    /**
     * Returns the eligibility response from Compliance.
     * @throws ComplianceCallException on any I/O or HTTP error
     */
    EligibilityApiResponse checkEligibility(UUID personId, UUID asdId, boolean agonistic) {
        try {
            var response = restClient.get()
                    .uri("/compliance/persons/{id}/eligibility?asdId={asdId}&agonistic={ag}",
                            personId, asdId, agonistic)
                    .retrieve()
                    .body(EligibilityApiResponse.class);

            if (response == null) throw new ComplianceCallException("Null response from Compliance");
            return response;

        } catch (RestClientException ex) {
            log.error("[COMPLIANCE-CLIENT] Unreachable for personId={}: {}", personId, ex.getMessage());
            throw new ComplianceCallException("Compliance unreachable: " + ex.getMessage());
        }
    }

    /** Record mirrors the JSON shape of EligibilityResponse from compliance-service. */
    record EligibilityApiResponse(boolean eligible, List<String> blockingDocuments, List<String> warnings) {}

    static final class ComplianceCallException extends RuntimeException {
        ComplianceCallException(String msg) { super(msg); }
    }
}
