package it.asd.compliance.features.uploaddocument;

import it.asd.common.exception.ApiErrors;
import it.asd.common.validation.annotation.ValidUUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Validated
@RestController
@RequestMapping("/compliance/persons/{personId}/documents")
public class UploadDocumentController {

    private final UploadDocumentHandler handler;

    public UploadDocumentController(UploadDocumentHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> upload(
            @PathVariable @ValidUUID UUID personId,
            @Valid @RequestBody UploadDocumentCommand cmd) {

        // Enforce URL person matches body person
        var effectiveCmd = new UploadDocumentCommand(
                personId, cmd.asdId(), cmd.tipo(), cmd.dataRilascio(),
                cmd.dataScadenza(), cmd.numero(), cmd.enteRilascio(),
                cmd.fileUrl(), cmd.note());

        // Exhaustive switch — sealed result forces handling all cases
        return switch (handler.handle(effectiveCmd)) {
            case UploadDocumentResult.Success s -> ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new UploadDocumentResponse(
                            s.documentId(), s.personId(), s.asdId(),
                            s.documentType(), null, s.dataScadenza(), "VALID"));

            case UploadDocumentResult.InvalidDateRange e -> {
                ProblemDetail problemDetail = ApiErrors.of(UNPROCESSABLE_ENTITY,
                        ApiErrors.INVALID_DATE_RANGE,
                        "Invalid date range: " + "Data rilascio - "
                                + e.dataRilascio() + "Data scadenza - " + e.dataScadenza());
                yield ResponseEntity.unprocessableEntity().body(problemDetail);
            }
        };
    }
}
