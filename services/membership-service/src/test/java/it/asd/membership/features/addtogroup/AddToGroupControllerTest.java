package it.asd.membership.features.addtogroup;

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

@WebMvcTest(AddToGroupController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("AddToGroupController")
@Tag("unit")
class AddToGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddToGroupHandler handler;

    private static final UUID GROUP_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private static final String VALID_BODY = """
            {
              "personId": "11111111-1111-1111-1111-111111111111",
              "groupId": "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
              "seasonId": "22222222-2222-2222-2222-222222222222",
              "ruolo": "Atleta",
              "dataIngresso": "2024-09-15"
            }
            """;

    @Test
    @DisplayName("POST returns 201 when handler returns Added")
    void returns201OnSuccess() throws Exception {
        var enrollmentId = UUID.randomUUID();
        when(handler.handle(any()))
                .thenReturn(new AddToGroupResult.Added(enrollmentId));

        mockMvc.perform(post("/membership/groups/{groupId}/members", GROUP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.enrollmentId").value(enrollmentId.toString()));
    }

    @Test
    @DisplayName("POST returns 404 when handler returns GroupNotFound")
    void returns404OnGroupNotFound() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new AddToGroupResult.GroupNotFound(GROUP_ID));

        mockMvc.perform(post("/membership/groups/{groupId}/members", GROUP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GROUP_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST returns 422 when handler returns NotAMember")
    void returns422OnNotAMember() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new AddToGroupResult.NotAMember("Person has no active membership for this season"));

        mockMvc.perform(post("/membership/groups/{groupId}/members", GROUP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("NOT_A_MEMBER"));
    }

    @Test
    @DisplayName("POST returns 409 when handler returns AlreadyInGroup")
    void returns409OnAlreadyInGroup() throws Exception {
        var existingEnrollmentId = UUID.randomUUID();
        when(handler.handle(any()))
                .thenReturn(new AddToGroupResult.AlreadyInGroup(existingEnrollmentId));

        mockMvc.perform(post("/membership/groups/{groupId}/members", GROUP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_IN_GROUP"));
    }

    @Test
    @DisplayName("POST returns 400 with VALIDATION_FAILED when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/membership/groups/{groupId}/members", GROUP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
