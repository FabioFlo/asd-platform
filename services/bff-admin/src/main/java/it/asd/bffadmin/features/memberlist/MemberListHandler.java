package it.asd.bffadmin.features.memberlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Member list handler.
 *
 * Strategy:
 * 1. Fetch paginated member list from membership-service.
 * 2. For each member, fetch compliance status from compliance-service
 *    (using the person_eligibility_cache endpoint — no heavy doc scan).
 * 3. Zip and return.
 *
 * Compliance calls are fanned out in parallel per page (max 20 members).
 * If compliance-service is down, all rows get complianceStatus = UNKNOWN.
 *
 * TODO: implement full body once membership-service /summary and
 *       compliance-service /persons/{id}/status endpoints are defined.
 */
@Component
public class MemberListHandler {

    private static final Logger log = LoggerFactory.getLogger(MemberListHandler.class);

    private final WebClient membershipClient;
    private final WebClient complianceClient;

    public MemberListHandler(
            @Qualifier("membershipWebClient") WebClient membershipClient,
            @Qualifier("complianceWebClient") WebClient complianceClient) {
        this.membershipClient = membershipClient;
        this.complianceClient = complianceClient;
    }

    public MemberListView handle(MemberListQuery query) {
        // TODO: implement fan-out
        // Step 1: GET /membership/asds/{asdId}/members?season=&page=&size=&search=
        // Step 2: for each member row, GET /compliance/persons/{id}/status
        // Step 3: zip into MemberListView.MemberRow list
        throw new UnsupportedOperationException("TODO: implement MemberListHandler");
    }
}
