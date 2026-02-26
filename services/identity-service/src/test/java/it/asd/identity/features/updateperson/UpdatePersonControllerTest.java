package it.asd.identity.features.updateperson;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UpdatePersonController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("UpdatePersonController")
@Tag("unit")
class UpdatePersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UpdatePersonHandler handler;

    @Test
    @DisplayName("PATCH returns 200 when handler returns Updated")
    void returns200OnSuccess() throws Exception {
        var personId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new UpdatePersonResult.Updated(personId));

        mockMvc.perform(patch("/identity/persons/{id}", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "personId": "%s",
                                  "nome": "Mario Updated"
                                }
                                """.formatted(personId)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH returns 404 when handler returns NotFound")
    void returns404OnNotFound() throws Exception {
        var personId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new UpdatePersonResult.NotFound(personId));

        mockMvc.perform(patch("/identity/persons/{id}", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "personId": "%s",
                                  "nome": "Mario Updated"
                                }
                                """.formatted(personId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PERSON_NOT_FOUND"));
    }

    @Test
    @DisplayName("PATCH returns 422 when handler returns DuplicateEmail")
    void returns422OnDuplicateEmail() throws Exception {
        var personId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new UpdatePersonResult.DuplicateEmail("taken@example.com"));

        mockMvc.perform(patch("/identity/persons/{id}", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "personId": "%s",
                                  "email": "taken@example.com"
                                }
                                """.formatted(personId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));
    }
}
