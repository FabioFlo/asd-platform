package it.asd.finance.features.overduescan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OverdueScanScheduler {

    private static final Logger log = LoggerFactory.getLogger(OverdueScanScheduler.class);

    private final OverdueScanHandler handler;

    public OverdueScanScheduler(OverdueScanHandler handler) {
        this.handler = handler;
    }

    @Scheduled(cron = "0 0 7 * * *")
    public void runDailyScan() {
        log.info("[OVERDUE_SCAN_SCHEDULER] Starting daily overdue scan");
        var result = handler.scan();
        switch (result) {
            case OverdueScanResult.Summary s ->
                log.info("[OVERDUE_SCAN_SCHEDULER] Completed: markedOverdue={} failed={}",
                        s.markedOverdue(), s.failed().size());
        }
    }
}
