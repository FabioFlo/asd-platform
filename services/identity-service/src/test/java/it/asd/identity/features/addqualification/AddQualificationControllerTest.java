package it.asd.identity.features.addqualification;

import it.asd.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddQualificationController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("AddQualificationController")
@Tag("unit")
class AddQualificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddQualificationHandler handler;

    @Test
    @DisplayName("POST returns 201 when handler returns Added")
    void returns201OnSuccess() throws Exception {
        var personId = UUID.randomUUID();
        var qualificationId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new AddQualificationResult.Added(qualificationId));

        mockMvc.perform(post("/identity/persons/{personId}/qualifications", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "personId": "%s",
                                  "tipo": "Istruttore Nuoto",
                                  "ente": "FIN",
                                  "livello": "1° Livello"
                                }
                                """.formatted(personId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.qualificationId").value(qualificationId.toString()))
                .andExpect(jsonPath("$.personId").value(personId.toString()));
    }

    @Test
    @DisplayName("POST returns 404 when handler returns PersonNotFound")
    void returns404OnPersonNotFound() throws Exception {
        var personId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new AddQualificationResult.PersonNotFound(personId));

        mockMvc.perform(post("/identity/persons/{personId}/qualifications", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "personId": "%s",
                                  "tipo": "Istruttore Nuoto",
                                  "ente": "FIN",
                                  "livello": "1° Livello"
                                }
                                """.formatted(personId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PERSON_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST returns 400 when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        var personId = UUID.randomUUID();

        mockMvc.perform(post("/identity/persons/{personId}/qualifications", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
