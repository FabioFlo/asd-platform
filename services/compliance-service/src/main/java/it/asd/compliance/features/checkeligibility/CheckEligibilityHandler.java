package it.asd.compliance.features.checkeligibility;

import it.asd.compliance.shared.entity.DocumentType;
import it.asd.compliance.shared.repository.DocumentRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Set;

/**
 * Handles eligibility checks for the Compliance service.
 * <p>
 * FAIL-CLOSED CONTRACT (enforced by caller — ComplianceClient in competition-service):
 * If this endpoint is unreachable, the competition service MUST deny registration.
 * This handler never needs to know about that — it just checks the documents.
 * <p>
 * Uses read-only transaction: no writes, just queries.
 */
@Component
public class CheckEligibilityHandler {

    private static final int EXPIRY_WARNING_DAYS = 30;

    private final DocumentRepository repository;

    public CheckEligibilityHandler(DocumentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public EligibilityResult handle(CheckEligibilityQuery query) {
        Set<DocumentType> required = query.agonistic()
                ? DocumentType.AGONISTIC_REQUIRED
                : DocumentType.RECREATIONAL_REQUIRED;

        var blocking = new ArrayList<String>();
        var expiringSoon = new ArrayList<String>();

        for (var type : required) {
            var docOpt = repository.findActiveByPersonAsdAndType(
                    query.personId(), query.asdId(), type);

            if (docOpt.isEmpty()) {
                blocking.add(type.name() + " [MISSING]");
            } else {
                var doc = docOpt.get();
                if (doc.isExpired()) {
                    blocking.add(type.name() + " [EXPIRED on " + doc.getDataScadenza() + "]");
                } else if (doc.isExpiringSoon(EXPIRY_WARNING_DAYS)) {
                    expiringSoon.add(type.name() + " [EXPIRING on " + doc.getDataScadenza() + "]");
                }
            }
        }

        if (!blocking.isEmpty()) return new EligibilityResult.Ineligible(blocking);
        if (!expiringSoon.isEmpty()) return new EligibilityResult.ExpiringSoon(expiringSoon);
        return new EligibilityResult.Eligible();
    }
}
