package it.asd.scheduling.features.addroom;

import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AddRoomCommand(
        @ValidUUID UUID venueId,
        @NotBlank String nome,
        Integer capienza,
        String note
) {
}
