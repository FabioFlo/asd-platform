package it.asd.scheduling.features.schedulesession;

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

@WebMvcTest(ScheduleSessionController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("ScheduleSessionController")
@Tag("unit")
class ScheduleSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScheduleSessionHandler handler;

    private static final String VALID_BODY = """
            {
              "asdId":     "00000000-0000-0000-0000-000000000001",
              "venueId":   "00000000-0000-0000-0000-000000000002",
              "titolo":    "Allenamento Squadra A",
              "data":      "2026-06-15",
              "oraInizio": "10:00:00",
              "oraFine":   "12:00:00",
              "tipo":      "TRAINING"
            }
            """;

    @Test
    @DisplayName("POST returns 201 with sessionId when handler returns Scheduled")
    void returns201OnScheduled() throws Exception {
        var sessionId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new ScheduleSessionResult.Scheduled(sessionId));

        mockMvc.perform(post("/scheduling/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sessionId").value(sessionId.toString()));
    }

    @Test
    @DisplayName("POST returns 404 with code VENUE_NOT_FOUND when handler returns VenueNotFound")
    void returns404OnVenueNotFound() throws Exception {
        when(handler.handle(any())).thenReturn(new ScheduleSessionResult.VenueNotFound());

        mockMvc.perform(post("/scheduling/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VENUE_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST returns 404 with code ROOM_NOT_FOUND when handler returns RoomNotFound")
    void returns404OnRoomNotFound() throws Exception {
        when(handler.handle(any())).thenReturn(new ScheduleSessionResult.RoomNotFound());

        mockMvc.perform(post("/scheduling/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ROOM_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST returns 404 with code GROUP_NOT_FOUND when handler returns GroupNotFound")
    void returns404OnGroupNotFound() throws Exception {
        when(handler.handle(any())).thenReturn(new ScheduleSessionResult.GroupNotFound());

        mockMvc.perform(post("/scheduling/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("GROUP_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST returns 409 with code TIME_CONFLICT when handler returns TimeConflict")
    void returns409OnTimeConflict() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new ScheduleSessionResult.TimeConflict("Room occupied 09:00–11:00"));

        mockMvc.perform(post("/scheduling/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TIME_CONFLICT"));
    }

    @Test
    @DisplayName("POST returns 422 with code INVALID_DATE_RANGE when handler returns InvalidTimeRange")
    void returns422OnInvalidTimeRange() throws Exception {
        when(handler.handle(any())).thenReturn(new ScheduleSessionResult.InvalidTimeRange());

        mockMvc.perform(post("/scheduling/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_DATE_RANGE"));
    }

    @Test
    @DisplayName("POST returns 400 with code VALIDATION_FAILED when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/scheduling/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
