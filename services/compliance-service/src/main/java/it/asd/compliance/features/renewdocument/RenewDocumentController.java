package it.asd.compliance.features.renewdocument;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/compliance/documents/{documentId}/renew")
public class RenewDocumentController {

    private final RenewDocumentHandler handler;

    public RenewDocumentController(RenewDocumentHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> renew(
            @PathVariable UUID documentId,
            @Valid @RequestBody RenewDocumentCommand cmd) {

        var effectiveCmd = new RenewDocumentCommand(
                documentId, cmd.newDataRilascio(), cmd.newDataScadenza(),
                cmd.newNumero(), cmd.newFileUrl());

        return switch (handler.handle(effectiveCmd)) {
            case RenewDocumentResult.Renewed r ->
                    ResponseEntity.ok().body(r);

            case RenewDocumentResult.NotFound nf -> {
                var pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
                pd.setType(URI.create("https://asd.it/errors/not-found"));
                pd.setDetail("Document not found: " + nf.documentId());
                yield ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd);
            }

            case RenewDocumentResult.InvalidDateRange e -> {
                var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
                pd.setType(URI.create("https://asd.it/errors/invalid-date-range"));
                pd.setDetail(e.reason());
                yield ResponseEntity.unprocessableEntity().body(pd);
            }
        };
    }
}
