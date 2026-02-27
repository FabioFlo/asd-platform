package it.asd.bffmember.features.documents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Documents handler — aggregation logic goes here.
 * TODO: inject required WebClient beans and implement fan-out.
 */
@Component
public class DocumentsHandler {

    private static final Logger log = LoggerFactory.getLogger(DocumentsHandler.class);

    public Object handle(Object query) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO: implement DocumentsHandler");
    }
}
