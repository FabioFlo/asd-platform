package it.asd.finance.features.overduescan;

import java.util.List;
import java.util.UUID;

public sealed interface OverdueScanResult
        permits OverdueScanResult.Summary {

    record Summary(int markedOverdue, List<UUID> failed) implements OverdueScanResult {}
}
