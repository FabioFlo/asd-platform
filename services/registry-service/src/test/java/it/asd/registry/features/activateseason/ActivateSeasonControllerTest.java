package it.asd.registry.features.activateseason;

import it.asd.common.exception.GlobalExceptionHandler;
import it.asd.common.exception.ValidatorExceptionHandler;
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

@WebMvcTest(ActivateSeasonController.class)
@Import({GlobalExceptionHandler.class, ValidatorExceptionHandler.class})
@DisplayName("ActivateSeasonController")
@Tag("unit")
class ActivateSeasonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivateSeasonHandler handler;

    @Test
    @DisplayName("POST returns 201 when handler returns Activated")
    void returns201OnSuccess() throws Exception {
        var asdId = UUID.randomUUID();
        var seasonId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new ActivateSeasonResult.Activated(seasonId, "2025-2026"));

        mockMvc.perform(post("/registry/asd/{asdId}/seasons", asdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "asdId": "%s",
                                  "codice": "2025-2026",
                                  "dataInizio": "2025-09-01",
                                  "dataFine": "2026-06-30"
                                }
                                """.formatted(asdId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.seasonId").value(seasonId.toString()))
                .andExpect(jsonPath("$.codice").value("2025-2026"));
    }

    @Test
    @DisplayName("POST returns 404 when handler returns AsdNotFound")
    void returns404OnAsdNotFound() throws Exception {
        var asdId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new ActivateSeasonResult.AsdNotFound(asdId));

        mockMvc.perform(post("/registry/asd/{asdId}/seasons", asdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "asdId": "%s",
                                  "codice": "2025-2026",
                                  "dataInizio": "2025-09-01",
                                  "dataFine": "2026-06-30"
                                }
                                """.formatted(asdId)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ASD_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST returns 409 when handler returns AlreadyHasActiveSeason")
    void returns409OnAlreadyActive() throws Exception {
        var asdId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new ActivateSeasonResult.AlreadyHasActiveSeason("2024-2025"));

        mockMvc.perform(post("/registry/asd/{asdId}/seasons", asdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "asdId": "%s",
                                  "codice": "2025-2026",
                                  "dataInizio": "2025-09-01",
                                  "dataFine": "2026-06-30"
                                }
                                """.formatted(asdId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ALREADY_ACTIVE_SEASON"));
    }

    @Test
    @DisplayName("POST returns 422 when handler returns InvalidDateRange")
    void returns422OnInvalidDateRange() throws Exception {
        var asdId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new ActivateSeasonResult.InvalidDateRange("invalid"));

        mockMvc.perform(post("/registry/asd/{asdId}/seasons", asdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "asdId": "%s",
                                  "codice": "2025-2026",
                                  "dataInizio": "2026-09-01",
                                  "dataFine": "2025-06-30"
                                }
                                """.formatted(asdId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_DATE_RANGE"));
    }

    @Test
    @DisplayName("POST returns 400 when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        var asdId = UUID.randomUUID();

        mockMvc.perform(post("/registry/asd/{asdId}/seasons", asdId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
