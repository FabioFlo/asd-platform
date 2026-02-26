package it.asd.identity.features.getperson;

import it.asd.common.exception.GlobalExceptionHandler;
import it.asd.identity.shared.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GetPersonController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("GetPersonController")
@Tag("unit")
class GetPersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetPersonHandler handler;

    @Test
    @DisplayName("GET returns 200 with person when handler returns Found")
    void returns200OnFound() throws Exception {
        var personId = UUID.randomUUID();
        var response = TestFixtures.personResponse(personId);
        when(handler.handle(any())).thenReturn(new GetPersonResult.Found(response));

        mockMvc.perform(get("/identity/persons/{personId}", personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(personId.toString()))
                .andExpect(jsonPath("$.codiceFiscale").value("RSSMRA80A01H501Z"));
    }

    @Test
    @DisplayName("GET returns 404 when handler returns NotFound")
    void returns404OnNotFound() throws Exception {
        var personId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new GetPersonResult.NotFound(personId));

        mockMvc.perform(get("/identity/persons/{personId}", personId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PERSON_NOT_FOUND"));
    }
}
