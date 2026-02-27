package it.asd.registry.features.getcurrentseason;

import it.asd.common.exception.GlobalExceptionHandler;
import it.asd.common.exception.ValidatorExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GetCurrentSeasonController.class)
@Import({GlobalExceptionHandler.class, ValidatorExceptionHandler.class})
@DisplayName("GetCurrentSeasonController")
@Tag("unit")
class GetCurrentSeasonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetCurrentSeasonHandler handler;

    @Test
    @DisplayName("GET returns 200 with season when handler returns Found")
    void returns200OnFound() throws Exception {
        var asdId = UUID.randomUUID();
        var seasonId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new GetCurrentSeasonResult.Found(
                seasonId, "2025-2026",
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2026, 6, 30)));

        mockMvc.perform(get("/registry/asd/{asdId}/season/current", asdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seasonId").value(seasonId.toString()))
                .andExpect(jsonPath("$.codice").value("2025-2026"));
    }

    @Test
    @DisplayName("GET returns 404 when handler returns NoActiveSeason")
    void returns404OnNoActiveSeason() throws Exception {
        var asdId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new GetCurrentSeasonResult.NoActiveSeason(asdId));

        mockMvc.perform(get("/registry/asd/{asdId}/season/current", asdId))
                .andExpect(status().isNotFound());
    }
}
