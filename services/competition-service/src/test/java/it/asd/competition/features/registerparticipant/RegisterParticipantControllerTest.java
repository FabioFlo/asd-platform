package it.asd.competition.features.registerparticipant;

import it.asd.common.exception.GlobalExceptionHandler;
import it.asd.common.exception.ValidatorExceptionHandler;
import it.asd.competition.shared.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RegisterParticipantController.class)
@Import({GlobalExceptionHandler.class, ValidatorExceptionHandler.class})
@DisplayName("RegisterParticipantController")
@Tag("unit")
class RegisterParticipantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RegisterParticipantHandler handler;

    private static final UUID EVENT_ID = TestFixtures.EVENT_ID;
    private static final UUID ASD_ID = TestFixtures.ASD_ID;
    private static final UUID SEASON_ID = TestFixtures.SEASON_ID;
    private static final UUID PERSON_ID = TestFixtures.PERSON_ID;

    // ── Registered → 201 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("POST returns 201 when handler returns Registered")
    void returns201OnRegistered() throws Exception {
        var participationId = UUID.randomUUID();
        when(handler.handle(any()))
                .thenReturn(new RegisterParticipantResult.Registered(participationId));

        mockMvc.perform(post("/competition/events/{eventId}/participants", EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "%s",
                                  "asdId": "%s",
                                  "seasonId": "%s",
                                  "personId": "%s",
                                  "agonistic": true
                                }
                                """.formatted(EVENT_ID, ASD_ID, SEASON_ID, PERSON_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.participationId").value(participationId.toString()))
                .andExpect(jsonPath("$.eventId").value(EVENT_ID.toString()));
    }

    // ── Ineligible → 422 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("POST returns 422 when handler returns Ineligible")
    void returns422OnIneligible() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new RegisterParticipantResult.Ineligible(
                        List.of("CERTIFICATO_MEDICO_AGONISTICO [EXPIRED on 2024-01-01]")));

        mockMvc.perform(post("/competition/events/{eventId}/participants", EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "%s",
                                  "asdId": "%s",
                                  "seasonId": "%s",
                                  "personId": "%s",
                                  "agonistic": true
                                }
                                """.formatted(EVENT_ID, ASD_ID, SEASON_ID, PERSON_ID)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("PERSON_INELIGIBLE"));
    }

    // ── AlreadyRegistered → 409 ───────────────────────────────────────────────

    @Test
    @DisplayName("POST returns 409 when handler returns AlreadyRegistered")
    void returns409OnAlreadyRegistered() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new RegisterParticipantResult.AlreadyRegistered(UUID.randomUUID()));

        mockMvc.perform(post("/competition/events/{eventId}/participants", EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "%s",
                                  "asdId": "%s",
                                  "seasonId": "%s",
                                  "personId": "%s",
                                  "agonistic": false
                                }
                                """.formatted(EVENT_ID, ASD_ID, SEASON_ID, PERSON_ID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_REGISTERED"));
    }

    // ── ComplianceUnavailable → 503 ───────────────────────────────────────────

    @Test
    @DisplayName("POST returns 503 with Retry-After when handler returns ComplianceUnavailable")
    void returns503OnComplianceUnavailable() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new RegisterParticipantResult.ComplianceUnavailable(
                        "Compliance unreachable: Connection refused"));

        mockMvc.perform(post("/competition/events/{eventId}/participants", EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eventId": "%s",
                                  "asdId": "%s",
                                  "seasonId": "%s",
                                  "personId": "%s",
                                  "agonistic": true
                                }
                                """.formatted(EVENT_ID, ASD_ID, SEASON_ID, PERSON_ID)))
                .andExpect(status().isServiceUnavailable())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.code").value("SERVICE_UNAVAILABLE"));
    }

    // ── Validation failure → 400 ──────────────────────────────────────────────

    @Test
    @DisplayName("POST returns 400 when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/competition/events/{eventId}/participants", EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
