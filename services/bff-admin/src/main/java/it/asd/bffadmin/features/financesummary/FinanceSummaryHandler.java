package it.asd.bffadmin.features.financesummary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * FinanceSummary handler — aggregation logic goes here.
 * TODO: inject required WebClient beans and implement fan-out.
 */
@Component
public class FinanceSummaryHandler {

    private static final Logger log = LoggerFactory.getLogger(FinanceSummaryHandler.class);

    public Object handle(Object query) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO: implement FinanceSummaryHandler");
    }
}
