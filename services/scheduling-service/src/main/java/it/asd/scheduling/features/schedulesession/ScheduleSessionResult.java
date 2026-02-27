package it.asd.scheduling.features.schedulesession;

import java.util.UUID;

public sealed interface ScheduleSessionResult
        permits ScheduleSessionResult.Scheduled,
        ScheduleSessionResult.VenueNotFound,
        ScheduleSessionResult.RoomNotFound,
        ScheduleSessionResult.GroupNotFound,
        ScheduleSessionResult.TimeConflict,
        ScheduleSessionResult.InvalidTimeRange {

    record Scheduled(UUID sessionId) implements ScheduleSessionResult {
    }

    record VenueNotFound() implements ScheduleSessionResult {
    }

    record RoomNotFound() implements ScheduleSessionResult {
    }

    record GroupNotFound() implements ScheduleSessionResult {
    }

    record TimeConflict(String detail) implements ScheduleSessionResult {
    }

    record InvalidTimeRange() implements ScheduleSessionResult {
    }
}
