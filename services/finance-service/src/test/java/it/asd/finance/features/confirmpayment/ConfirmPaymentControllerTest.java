package it.asd.finance.features.confirmpayment;

import it.asd.common.exception.GlobalExceptionHandler;
import it.asd.common.exception.ValidatorExceptionHandler;
import it.asd.finance.shared.TestFixtures;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ConfirmPaymentController.class)
@Import({GlobalExceptionHandler.class, ValidatorExceptionHandler.class})
@DisplayName("ConfirmPaymentController")
@Tag("unit")
class ConfirmPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfirmPaymentHandler handler;

    private static final UUID PAYMENT_ID = TestFixtures.PAYMENT_ID;

    @Test
    @DisplayName("POST returns 200 with payment when handler returns Confirmed")
    void returns200OnConfirmed() throws Exception {
        when(handler.handle(any())).thenReturn(
                new ConfirmPaymentResult.Confirmed(PAYMENT_ID, new BigDecimal("50.00")));

        mockMvc.perform(post("/finance/payments/{paymentId}/confirm", PAYMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "77777777-7777-7777-7777-777777777777",
                                  "dataPagamento": "2026-02-01",
                                  "metodoPagamento": "BONIFICO",
                                  "riferimento": "RIF-001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(PAYMENT_ID.toString()))
                .andExpect(jsonPath("$.stato").value("CONFIRMED"));
    }

    @Test
    @DisplayName("POST returns 404 when handler returns NotFound")
    void returns404OnNotFound() throws Exception {
        when(handler.handle(any())).thenReturn(new ConfirmPaymentResult.NotFound(PAYMENT_ID));

        mockMvc.perform(post("/finance/payments/{paymentId}/confirm", PAYMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "77777777-7777-7777-7777-777777777777",
                                  "dataPagamento": "2026-02-01",
                                  "metodoPagamento": "BONIFICO"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PAYMENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("POST returns 409 when handler returns AlreadyConfirmed")
    void returns409OnAlreadyConfirmed() throws Exception {
        when(handler.handle(any())).thenReturn(new ConfirmPaymentResult.AlreadyConfirmed());

        mockMvc.perform(post("/finance/payments/{paymentId}/confirm", PAYMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "77777777-7777-7777-7777-777777777777",
                                  "dataPagamento": "2026-02-01",
                                  "metodoPagamento": "BONIFICO"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PAYMENT_ALREADY_CONFIRMED"));
    }

    @Test
    @DisplayName("POST returns 409 when handler returns AlreadyCancelled")
    void returns409OnAlreadyCancelled() throws Exception {
        when(handler.handle(any())).thenReturn(new ConfirmPaymentResult.AlreadyCancelled());

        mockMvc.perform(post("/finance/payments/{paymentId}/confirm", PAYMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paymentId": "77777777-7777-7777-7777-777777777777",
                                  "dataPagamento": "2026-02-01",
                                  "metodoPagamento": "BONIFICO"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PAYMENT_ALREADY_CANCELLED"));
    }

    @Test
    @DisplayName("POST returns 400 when required fields are missing")
    void returns400OnValidationFailure() throws Exception {
        mockMvc.perform(post("/finance/payments/{paymentId}/confirm", PAYMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fields").isArray());
    }
}
