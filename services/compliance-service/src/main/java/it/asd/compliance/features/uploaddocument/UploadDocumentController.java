package it.asd.compliance.features.uploaddocument;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/compliance/persons/{personId}/documents")
public class UploadDocumentController {

    private final UploadDocumentHandler handler;

    public UploadDocumentController(UploadDocumentHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<?> upload(
            @PathVariable UUID personId,
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
                var pd = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
                pd.setType(URI.create("https://asd.it/errors/invalid-date-range"));
                pd.setDetail(e.reason());
                pd.setProperty("dataRilascio", e.dataRilascio());
                pd.setProperty("dataScadenza", e.dataScadenza());
                yield ResponseEntity.unprocessableEntity().body(pd);
            }
        };
    }
}
