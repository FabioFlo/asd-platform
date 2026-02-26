package it.asd.competition.features.recordresult;

import it.asd.common.exception.GlobalExceptionHandler;
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

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecordResultController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("RecordResultController")
@Tag("unit")
class RecordResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecordResultHandler handler;

    private static final UUID PARTICIPATION_ID = TestFixtures.PARTICIPATION_ID;
    private static final UUID PERSON_ID        = TestFixtures.PERSON_ID;

    // ── Recorded → 200 ───────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT returns 200 when handler returns Recorded")
    void returns200OnRecorded() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new RecordResultResult.Recorded(
                        PARTICIPATION_ID,
                        PERSON_ID,
                        null,
                        null,
                        1,
                        BigDecimal.valueOf(9.85),
                        Map.of("notes", "personal record")));

        mockMvc.perform(put("/competition/participants/{participationId}/result", PARTICIPATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participationId": "%s",
                                  "posizione": 1,
                                  "punteggio": 9.85
                                }
                                """.formatted(PARTICIPATION_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participationId").value(PARTICIPATION_ID.toString()))
                .andExpect(jsonPath("$.stato").value("PARTICIPATED"));
    }

    // ── NotFound → 404 ───────────────────────────────────────────────────────

    @Test
    @DisplayName("PUT returns 404 when handler returns NotFound")
    void returns404OnNotFound() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new RecordResultResult.NotFound(PARTICIPATION_ID));

        mockMvc.perform(put("/competition/participants/{participationId}/result", PARTICIPATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "participationId": "%s",
                                  "posizione": 1
                                }
                                """.formatted(PARTICIPATION_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PARTICIPATION_NOT_FOUND"));
    }

    // ── Validation failure → 400 ──────────────────────────────────────────────

    @Test
    @DisplayName("PUT returns 400 when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        mockMvc.perform(put("/competition/participants/{participationId}/result", PARTICIPATION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
