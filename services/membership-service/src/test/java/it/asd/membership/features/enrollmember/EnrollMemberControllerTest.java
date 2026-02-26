package it.asd.membership.features.enrollmember;

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

@WebMvcTest(EnrollMemberController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("EnrollMemberController")
@Tag("unit")
class EnrollMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EnrollMemberHandler handler;

    private static final String VALID_BODY = """
            {
              "personId": "11111111-1111-1111-1111-111111111111",
              "asdId": "22222222-2222-2222-2222-222222222222",
              "seasonId": "33333333-3333-3333-3333-333333333333",
              "dataIscrizione": "2024-09-01",
              "note": "Test enrollment"
            }
            """;

    @Test
    @DisplayName("POST returns 201 when handler returns Enrolled")
    void returns201OnSuccess() throws Exception {
        var membershipId = UUID.randomUUID();
        when(handler.handle(any()))
                .thenReturn(new EnrollMemberResult.Enrolled(membershipId, "ABCD-2024-12345678"));

        mockMvc.perform(post("/membership/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.membershipId").value(membershipId.toString()))
                .andExpect(jsonPath("$.numeroTessera").value("ABCD-2024-12345678"));
    }

    @Test
    @DisplayName("POST returns 409 when handler returns AlreadyEnrolled")
    void returns409OnAlreadyEnrolled() throws Exception {
        var existingId = UUID.randomUUID();
        when(handler.handle(any()))
                .thenReturn(new EnrollMemberResult.AlreadyEnrolled(existingId));

        mockMvc.perform(post("/membership/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_ENROLLED"));
    }

    @Test
    @DisplayName("POST returns 404 when handler returns PersonNotFound")
    void returns404OnPersonNotFound() throws Exception {
        var personId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        when(handler.handle(any()))
                .thenReturn(new EnrollMemberResult.PersonNotFound(personId));

        mockMvc.perform(post("/membership/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PERSON_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST returns 404 when handler returns AsdNotFound")
    void returns404OnAsdNotFound() throws Exception {
        var asdId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(handler.handle(any()))
                .thenReturn(new EnrollMemberResult.AsdNotFound(asdId));

        mockMvc.perform(post("/membership/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ASD_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST returns 404 when handler returns SeasonNotFound")
    void returns404OnSeasonNotFound() throws Exception {
        var asdId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(handler.handle(any()))
                .thenReturn(new EnrollMemberResult.SeasonNotFound(asdId));

        mockMvc.perform(post("/membership/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SEASON_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST returns 400 with VALIDATION_FAILED when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/membership/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
