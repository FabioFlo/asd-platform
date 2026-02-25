package it.asd.finance.features.confirmpayment;

import java.math.BigDecimal;
import java.util.UUID;

public sealed interface ConfirmPaymentResult
        permits ConfirmPaymentResult.Confirmed,
                ConfirmPaymentResult.NotFound,
                ConfirmPaymentResult.AlreadyConfirmed,
                ConfirmPaymentResult.AlreadyCancelled {

    record Confirmed(UUID paymentId, BigDecimal importo) implements ConfirmPaymentResult {}
    record NotFound(UUID paymentId) implements ConfirmPaymentResult {}
    record AlreadyConfirmed() implements ConfirmPaymentResult {}
    record AlreadyCancelled() implements ConfirmPaymentResult {}
}
