package it.asd.scheduling.features.schedulesession;

import it.asd.scheduling.shared.entity.SessionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleSessionCommand(
        @NotNull UUID asdId,
        @NotNull UUID venueId,
        UUID roomId,
        UUID groupId,
        @NotBlank String titolo,
        @NotNull LocalDate data,
        @NotNull LocalTime oraInizio,
        @NotNull LocalTime oraFine,
        @NotNull SessionType tipo,
        String note
) {}
