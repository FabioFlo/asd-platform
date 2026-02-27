package it.asd.registry.features.createasd;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CreateAsdController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("CreateAsdController")
@Tag("unit")
class CreateAsdControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateAsdHandler handler;

    @Test
    @DisplayName("POST returns 201 when handler returns Created")
    void returns201OnSuccess() throws Exception {
        var asdId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new CreateAsdResult.Created(asdId, "ASD Test Nuoto"));

        mockMvc.perform(post("/registry/asd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codiceFiscale": "12345678901",
                                  "nome": "ASD Test Nuoto"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(asdId.toString()))
                .andExpect(jsonPath("$.stato").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST returns 422 when handler returns DuplicateCodiceFiscale")
    void returns422OnDuplicateCf() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new CreateAsdResult.DuplicateCodiceFiscale("12345678901"));

        mockMvc.perform(post("/registry/asd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codiceFiscale": "12345678901",
                                  "nome": "ASD Test Nuoto"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("DUPLICATE_CODICE_FISCALE"));
    }

    @Test
    @DisplayName("POST returns 400 when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/registry/asd")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
