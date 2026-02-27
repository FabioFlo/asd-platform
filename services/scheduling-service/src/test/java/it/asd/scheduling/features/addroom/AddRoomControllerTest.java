package it.asd.scheduling.features.addroom;

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

@WebMvcTest(AddRoomController.class)
@Import({GlobalExceptionHandler.class, ValidatorExceptionHandler.class})
@DisplayName("AddRoomController")
@Tag("unit")
class AddRoomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AddRoomHandler handler;

    private static final UUID VENUE_ID = UUID.fromString("00000000-0000-0000-0000-000000000002");

    private static final String VALID_BODY = """
            {
              "venueId": "00000000-0000-0000-0000-000000000002",
              "nome": "Sala Principale",
              "capienza": 50
            }
            """;

    @Test
    @DisplayName("POST returns 201 with roomId when handler returns Added")
    void returns201OnAdded() throws Exception {
        var roomId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new AddRoomResult.Added(roomId));

        mockMvc.perform(post("/scheduling/venues/{venueId}/rooms", VENUE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roomId").value(roomId.toString()));
    }

    @Test
    @DisplayName("POST returns 404 with code VENUE_NOT_FOUND when handler returns VenueNotFound")
    void returns404OnVenueNotFound() throws Exception {
        when(handler.handle(any())).thenReturn(new AddRoomResult.VenueNotFound(VENUE_ID));

        mockMvc.perform(post("/scheduling/venues/{venueId}/rooms", VENUE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("VENUE_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST returns 422 with code ROOM_NAME_EXISTS when handler returns DuplicateName")
    void returns422OnDuplicateName() throws Exception {
        when(handler.handle(any())).thenReturn(new AddRoomResult.DuplicateName("Sala Principale"));

        mockMvc.perform(post("/scheduling/venues/{venueId}/rooms", VENUE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ROOM_NAME_EXISTS"));
    }

    @Test
    @DisplayName("POST returns 400 with code VALIDATION_FAILED when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/scheduling/venues/{venueId}/rooms", VENUE_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
