package it.asd.bffmember.features.payments;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Payments handler — aggregation logic goes here.
 * TODO: inject required WebClient beans and implement fan-out.
 */
@Component
public class PaymentsHandler {

    private static final Logger log = LoggerFactory.getLogger(PaymentsHandler.class);

    public Object handle(Object query) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO: implement PaymentsHandler");
    }
}
