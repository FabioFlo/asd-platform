package it.asd.finance.features.confirmpayment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(UUID paymentId, BigDecimal importo, String stato) {
}
