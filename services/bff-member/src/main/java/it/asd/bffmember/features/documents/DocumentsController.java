package it.asd.bffmember.features.documents;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Documents controller — thin HTTP adapter.
 * TODO: replace Object with typed Query/View records.
 */
@RestController
@RequestMapping("/member/documents")
public class DocumentsController {

    private final DocumentsHandler handler;

    public DocumentsController(DocumentsHandler handler) {
        this.handler = handler;
    }

    @GetMapping
    @PreAuthorize("hasRole('ATLETA')")
    public ResponseEntity<Object> handle() {
        // TODO: extract params, call handler
        return ResponseEntity.ok(null);
    }
}
