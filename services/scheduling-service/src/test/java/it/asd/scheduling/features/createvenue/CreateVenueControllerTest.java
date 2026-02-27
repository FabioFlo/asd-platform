package it.asd.scheduling.features.createvenue;

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

@WebMvcTest(CreateVenueController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("CreateVenueController")
@Tag("unit")
class CreateVenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateVenueHandler handler;

    private static final String VALID_BODY = """
            {
              "asdId": "00000000-0000-0000-0000-000000000001",
              "nome": "Palazzetto dello Sport"
            }
            """;

    @Test
    @DisplayName("POST returns 201 with venueId when handler returns Created")
    void returns201OnCreated() throws Exception {
        var venueId = UUID.randomUUID();
        when(handler.handle(any())).thenReturn(new CreateVenueResult.Created(venueId));

        mockMvc.perform(post("/scheduling/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.venueId").value(venueId.toString()));
    }

    @Test
    @DisplayName("POST returns 422 with code ROOM_NAME_EXISTS when handler returns DuplicateName")
    void returns422OnDuplicateName() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new CreateVenueResult.DuplicateName("Palazzetto dello Sport"));

        mockMvc.perform(post("/scheduling/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ROOM_NAME_EXISTS"));
    }

    @Test
    @DisplayName("POST returns 400 with code VALIDATION_FAILED when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/scheduling/venues")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
