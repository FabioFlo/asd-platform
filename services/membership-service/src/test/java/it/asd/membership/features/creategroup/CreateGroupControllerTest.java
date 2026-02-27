package it.asd.membership.features.creategroup;

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

@WebMvcTest(CreateGroupController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("CreateGroupController")
@Tag("unit")
class CreateGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateGroupHandler handler;

    private static final String VALID_BODY = """
            {
              "asdId": "11111111-1111-1111-1111-111111111111",
              "seasonId": "22222222-2222-2222-2222-222222222222",
              "nome": "Nuoto Agonistico",
              "disciplina": "Nuoto",
              "tipo": "Agonistico",
              "note": "Test group"
            }
            """;

    @Test
    @DisplayName("POST returns 201 when handler returns Created")
    void returns201OnSuccess() throws Exception {
        var groupId = UUID.randomUUID();
        when(handler.handle(any()))
                .thenReturn(new CreateGroupResult.Created(groupId));

        mockMvc.perform(post("/membership/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.groupId").value(groupId.toString()));
    }

    @Test
    @DisplayName("POST returns 422 when handler returns DuplicateName")
    void returns422OnDuplicateName() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new CreateGroupResult.DuplicateName("Nuoto Agonistico"));

        mockMvc.perform(post("/membership/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("POST returns 400 with VALIDATION_FAILED when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/membership/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
