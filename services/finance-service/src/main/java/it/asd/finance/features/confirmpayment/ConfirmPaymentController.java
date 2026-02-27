package it.asd.finance.features.confirmpayment;

import it.asd.common.exception.ApiErrors;
import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@RestController
@RequestMapping("/finance/payments/{paymentId}/confirm")
public class ConfirmPaymentController {

    private final ConfirmPaymentHandler handler;

    public ConfirmPaymentController(ConfirmPaymentHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> confirm(
            @PathVariable @ValidUUID UUID paymentId,
            @Valid @RequestBody ConfirmPaymentCommand cmd) {

        var effectiveCmd = new ConfirmPaymentCommand(
                paymentId, cmd.dataPagamento(), cmd.metodoPagamento(), cmd.riferimento(), cmd.note());

        return switch (handler.handle(effectiveCmd)) {
            case ConfirmPaymentResult.Confirmed c -> ResponseEntity.ok(
                    new PaymentResponse(c.paymentId(), c.importo(), "CONFIRMED"));

            case ConfirmPaymentResult.NotFound n -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.NOT_FOUND, ApiErrors.PAYMENT_NOT_FOUND, "Payment not found: " + n.paymentId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
            }

            case ConfirmPaymentResult.AlreadyConfirmed _ -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.CONFLICT, ApiErrors.PAYMENT_ALREADY_CONFIRMED, "Payment already confirmed");
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
            }

            case ConfirmPaymentResult.AlreadyCancelled _ -> {
                ProblemDetail problemDetail = ApiErrors.of(HttpStatus.CONFLICT, ApiErrors.PAYMENT_ALREADY_CANCELLED, "Payment already cancelled");
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(problemDetail);
            }
        };
    }
}
