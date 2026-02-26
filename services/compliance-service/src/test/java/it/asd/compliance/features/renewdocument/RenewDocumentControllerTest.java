package it.asd.compliance.features.renewdocument;

import it.asd.common.exception.GlobalExceptionHandler;
import it.asd.compliance.shared.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RenewDocumentController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("RenewDocumentController")
@Tag("unit")
class RenewDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RenewDocumentHandler handler;

    @Test
    @DisplayName("POST returns 200 when handler returns Renewed")
    void returns200OnSuccess() throws Exception {
        var documentId = UUID.randomUUID();
        var renewed = new RenewDocumentResult.Renewed(
                documentId, TestFixtures.PERSON_ID, TestFixtures.ASD_ID,
                "CERTIFICATO_MEDICO_AGONISTICO", LocalDate.now().plusYears(1));
        when(handler.handle(any())).thenReturn(renewed);

        mockMvc.perform(post("/compliance/documents/{documentId}/renew", documentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "documentId": "%s",
                                  "newDataRilascio": "2025-01-01",
                                  "newDataScadenza": "2026-01-01"
                                }
                                """.formatted(documentId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value(documentId.toString()));
    }

    @Test
    @DisplayName("POST returns 404 when handler returns NotFound")
    void returns404OnNotFound() throws Exception {
        var documentId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new RenewDocumentResult.NotFound(documentId));

        mockMvc.perform(post("/compliance/documents/{documentId}/renew", documentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "documentId": "%s",
                                  "newDataRilascio": "2025-01-01",
                                  "newDataScadenza": "2026-01-01"
                                }
                                """.formatted(documentId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("DOCUMENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST returns 422 when handler returns InvalidDateRange")
    void returns422OnInvalidDateRange() throws Exception {
        var documentId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new RenewDocumentResult.InvalidDateRange("invalid"));

        mockMvc.perform(post("/compliance/documents/{documentId}/renew", documentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "documentId": "%s",
                                  "newDataRilascio": "2026-01-01",
                                  "newDataScadenza": "2025-01-01"
                                }
                                """.formatted(documentId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_DATE_RANGE"));
    }

    @Test
    @DisplayName("POST returns 400 when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        var documentId = UUID.randomUUID();

        mockMvc.perform(post("/compliance/documents/{documentId}/renew", documentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
