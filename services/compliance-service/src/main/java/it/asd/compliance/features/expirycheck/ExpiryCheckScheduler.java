package it.asd.compliance.features.expirycheck;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Drives the daily expiry check job.
 * Separated from the handler so the handler is easily testable without scheduling.
 */
@Component
public class ExpiryCheckScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiryCheckScheduler.class);

    private final ExpiryCheckHandler handler;

    public ExpiryCheckScheduler(ExpiryCheckHandler handler) {
        this.handler = handler;
    }

    @Scheduled(cron = "0 0 6 * * *")    // every day at 06:00
    public void runDailyExpiryCheck() {
        log.info("[SCHEDULER] Starting daily compliance expiry check");
        var result = handler.handle();

        // Exhaustive switch — if we add a new result type, this won't compile
        switch (result) {
            case ExpiryCheckResult.Summary s -> {
                log.info("[SCHEDULER] Done: expired={}, expiringSoon={}, failed={}",
                        s.expiredCount(), s.expiringSoonCount(), s.failedDocumentIds().size());
                if (s.hasFailures())
                    log.warn("[SCHEDULER] Failed document IDs: {}", s.failedDocumentIds());
            }
        }
    }
}
