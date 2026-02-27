package it.asd.finance.features.confirmpayment;

import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record ConfirmPaymentCommand(
        @ValidUUID UUID paymentId,
        @NotNull LocalDate dataPagamento,
        @NotBlank String metodoPagamento,
        String riferimento,
        String note
) {
}
