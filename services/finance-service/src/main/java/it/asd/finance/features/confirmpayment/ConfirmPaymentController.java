package it.asd.finance.features.confirmpayment;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/finance/payments/{paymentId}/confirm")
public class ConfirmPaymentController {

    private final ConfirmPaymentHandler handler;

    public ConfirmPaymentController(ConfirmPaymentHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> confirm(
            @PathVariable UUID paymentId,
            @Valid @RequestBody ConfirmPaymentCommand cmd) {

        var effectiveCmd = new ConfirmPaymentCommand(
                paymentId, cmd.dataPagamento(), cmd.metodoPagamento(), cmd.riferimento(), cmd.note());

        return switch (handler.handle(effectiveCmd)) {
            case ConfirmPaymentResult.Confirmed c -> ResponseEntity.ok(
                    new PaymentResponse(c.paymentId(), c.importo(), "CONFIRMED"));

            case ConfirmPaymentResult.NotFound n -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/payment-not-found"));
                pd.setDetail("Payment not found: " + n.paymentId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }

            case ConfirmPaymentResult.AlreadyConfirmed a -> {
                var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
                pd.setType(URI.create("https://asd.it/errors/already-confirmed"));
                pd.setDetail("Payment is already confirmed");
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
            }

            case ConfirmPaymentResult.AlreadyCancelled a -> {
                var pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
                pd.setType(URI.create("https://asd.it/errors/already-cancelled"));
                pd.setDetail("Payment is cancelled and cannot be confirmed");
                yield ResponseEntity.status(HttpStatus.CONFLICT).body(pd);
            }
        };
    }
}
