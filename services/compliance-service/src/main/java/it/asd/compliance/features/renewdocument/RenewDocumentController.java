package it.asd.compliance.features.renewdocument;

import it.asd.common.exception.ApiErrors;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

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

        RenewDocumentCommand effectiveCmd = new RenewDocumentCommand(
                documentId, cmd.newDataRilascio(), cmd.newDataScadenza(),
                cmd.newNumero(), cmd.newFileUrl());

        return switch (handler.handle(effectiveCmd)) {
            case RenewDocumentResult.Renewed r -> ResponseEntity.ok().body(r);

            case RenewDocumentResult.NotFound nf -> {
                ProblemDetail pd = ApiErrors.of(NOT_FOUND, ApiErrors.DOCUMENT_NOT_FOUND, "Document not found: " + nf.documentId());
                yield ResponseEntity.status(NOT_FOUND).body(pd);
            }

            case RenewDocumentResult.InvalidDateRange _ -> {
                ProblemDetail pd = ApiErrors.of(UNPROCESSABLE_ENTITY, ApiErrors.INVALID_DATE_RANGE, "Invalid date range: " + cmd.newDataRilascio());
                yield ResponseEntity.unprocessableEntity().body(pd);
            }
        };
    }
}
