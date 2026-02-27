package it.asd.compliance.features.uploaddocument;

import it.asd.common.exception.GlobalExceptionHandler;
import it.asd.common.exception.ValidatorExceptionHandler;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UploadDocumentController.class)
@Import({GlobalExceptionHandler.class, ValidatorExceptionHandler.class})
@DisplayName("UploadDocumentController")
@Tag("unit")
class UploadDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UploadDocumentHandler handler;

    @Test
    @DisplayName("POST returns 201 when handler returns Success")
    void returns201OnSuccess() throws Exception {
        var documentId = java.util.UUID.randomUUID();
        var result = new UploadDocumentResult.Success(
                documentId, TestFixtures.PERSON_ID, TestFixtures.ASD_ID,
                "CERTIFICATO_MEDICO_AGONISTICO", LocalDate.now().plusYears(1));
        when(handler.handle(any())).thenReturn(result);

        mockMvc.perform(post("/compliance/persons/{personId}/documents", TestFixtures.PERSON_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "personId": "%s",
                                  "asdId": "%s",
                                  "tipo": "CERTIFICATO_MEDICO_AGONISTICO",
                                  "dataRilascio": "2024-01-01",
                                  "dataScadenza": "2025-01-01"
                                }
                                """.formatted(TestFixtures.PERSON_ID, TestFixtures.ASD_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.stato").value("VALID"));
    }

    @Test
    @DisplayName("POST returns 422 when handler returns InvalidDateRange")
    void returns422OnInvalidDateRange() throws Exception {
        when(handler.handle(any())).thenReturn(
                new UploadDocumentResult.InvalidDateRange(
                        LocalDate.now(), LocalDate.now().minusDays(1), "invalid"));

        mockMvc.perform(post("/compliance/persons/{personId}/documents", TestFixtures.PERSON_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "personId": "%s",
                                  "asdId": "%s",
                                  "tipo": "CERTIFICATO_MEDICO_AGONISTICO",
                                  "dataRilascio": "2025-01-01",
                                  "dataScadenza": "2024-01-01"
                                }
                                """.formatted(TestFixtures.PERSON_ID, TestFixtures.ASD_ID)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_DATE_RANGE"));
    }

    @Test
    @DisplayName("POST returns 400 when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/compliance/persons/{personId}/documents", TestFixtures.PERSON_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
