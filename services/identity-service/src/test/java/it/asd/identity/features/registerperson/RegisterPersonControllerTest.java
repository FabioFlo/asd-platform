package it.asd.identity.features.registerperson;

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

@WebMvcTest(RegisterPersonController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("RegisterPersonController")
@Tag("unit")
class RegisterPersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterPersonHandler handler;

    @Test
    @DisplayName("POST returns 201 when handler returns Registered")
    void returns201OnSuccess() throws Exception {
        var personId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new RegisterPersonResult.Registered(personId));

        mockMvc.perform(post("/identity/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codiceFiscale": "RSSMRA80A01H501Z",
                                  "nome": "Mario",
                                  "cognome": "Rossi",
                                  "codiceProvinciaNascita": "H"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(personId.toString()));
    }

    @Test
    @DisplayName("POST returns 422 when handler returns DuplicateCodiceFiscale")
    void returns422OnDuplicateCf() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new RegisterPersonResult.DuplicateCodiceFiscale("RSSMRA80A01H501Z"));

        mockMvc.perform(post("/identity/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codiceFiscale": "RSSMRA80A01H501Z",
                                  "nome": "Mario",
                                  "cognome": "Rossi",
                                  "codiceProvinciaNascita": "H"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ALREADY_REGISTERED"));
    }

    @Test
    @DisplayName("POST returns 422 when handler returns DuplicateEmail")
    void returns422OnDuplicateEmail() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new RegisterPersonResult.DuplicateEmail("mario@example.com"));

        mockMvc.perform(post("/identity/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codiceFiscale": "RSSMRA80A01H501Z",
                                  "nome": "Mario",
                                  "cognome": "Rossi",
                                  "codiceProvinciaNascita": "H"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
    }

    @Test
    @DisplayName("POST returns 400 when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/identity/persons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codiceProvinciaNascita": "X"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
