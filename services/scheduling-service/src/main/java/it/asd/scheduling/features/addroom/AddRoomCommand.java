package it.asd.scheduling.features.addroom;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddRoomCommand(
        @NotNull UUID venueId,
        @NotBlank String nome,
        Integer capienza,
        String note
) {}
