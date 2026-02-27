package it.asd.bffmember.features.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Schedule handler — aggregation logic goes here.
 * TODO: inject required WebClient beans and implement fan-out.
 */
@Component
public class ScheduleHandler {

    private static final Logger log = LoggerFactory.getLogger(ScheduleHandler.class);

    public Object handle(Object query) {
        // TODO: implement
        throw new UnsupportedOperationException("TODO: implement ScheduleHandler");
    }
}
