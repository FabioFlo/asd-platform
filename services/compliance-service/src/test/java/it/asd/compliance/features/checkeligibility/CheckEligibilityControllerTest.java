package it.asd.compliance.features.checkeligibility;

import it.asd.common.exception.GlobalExceptionHandler;
import it.asd.compliance.shared.TestFixtures;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CheckEligibilityController.class)
@Import(GlobalExceptionHandler.class)
@DisplayName("CheckEligibilityController")
@Tag("unit")
class CheckEligibilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckEligibilityHandler handler;

    @Test
    @DisplayName("GET returns 200 with eligible=true when handler returns Eligible")
    void returns200WhenEligible() throws Exception {
        when(handler.handle(any())).thenReturn(new EligibilityResult.Eligible());

        mockMvc.perform(get("/compliance/persons/{personId}/eligibility", TestFixtures.PERSON_ID)
                        .param("asdId", TestFixtures.ASD_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(true))
                .andExpect(jsonPath("$.blockingDocuments").isArray())
                .andExpect(jsonPath("$.warnings").isArray());
    }

    @Test
    @DisplayName("GET returns 200 with eligible=false when handler returns Ineligible")
    void returns200WhenIneligible() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new EligibilityResult.Ineligible(List.of("CERTIFICATO_MEDICO_AGONISTICO [MISSING]")));

        mockMvc.perform(get("/compliance/persons/{personId}/eligibility", TestFixtures.PERSON_ID)
                        .param("asdId", TestFixtures.ASD_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(false))
                .andExpect(jsonPath("$.blockingDocuments").isArray());
    }

    @Test
    @DisplayName("GET returns 200 with eligible=true and warnings when handler returns ExpiringSoon")
    void returns200WhenExpiringSoon() throws Exception {
        when(handler.handle(any()))
                .thenReturn(new EligibilityResult.ExpiringSoon(List.of("ASSICURAZIONE [EXPIRING on 2025-02-28]")));

        mockMvc.perform(get("/compliance/persons/{personId}/eligibility", TestFixtures.PERSON_ID)
                        .param("asdId", TestFixtures.ASD_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eligible").value(true))
                .andExpect(jsonPath("$.warnings").isArray());
    }
}
