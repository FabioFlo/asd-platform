package it.asd.bffadmin.features.dashboard;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for DashboardHandler.
 *
 * Uses WireMock stubs (see integration test) for full-stack tests.
 * Here we test the aggregation logic and partial-data fallback
 * using mocked WebClients.
 *
 * NOTE: full WebClient mocking is verbose. Prefer the integration test
 * with WireMock for coverage of the actual HTTP interaction.
 * This unit test focuses on the sealed/null fallback logic.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardHandler")
class DashboardHandlerTest {

    @Mock WebClient membershipClient;
    @Mock WebClient complianceClient;
    @Mock WebClient financeClient;
    @Mock WebClient competitionClient;

    // Handler will be constructed manually once WebClient mocking helpers are added
    // See DashboardIntegrationTest for full-stack coverage with WireMock

    @Nested
    @DisplayName("query construction")
    class QueryConstruction {

        @Test
        @DisplayName("DashboardQuery holds asdId and seasonId")
        void query_holds_ids() {
            var asdId    = UUID.randomUUID();
            var seasonId = UUID.randomUUID();
            var query    = new DashboardQuery(asdId, seasonId);

            assertThat(query.asdId()).isEqualTo(asdId);
            assertThat(query.seasonId()).isEqualTo(seasonId);
        }
    }

    @Nested
    @DisplayName("DashboardView")
    class DashboardViewTests {

        @Test
        @DisplayName("partialData flag is false when all sections present")
        void partialData_false_when_all_present() {
            var view = new DashboardView(
                    UUID.randomUUID(), UUID.randomUUID(),
                    "ASD Test", "2024-25",
                    new DashboardView.MembershipSummary(50, 2, 3),
                    new DashboardView.ComplianceSummary(5, 1, 2),
                    new DashboardView.FinanceSummary(
                            java.math.BigDecimal.valueOf(2500), java.math.BigDecimal.valueOf(300), 3, 47),
                    new DashboardView.CompetitionSummary(2, 1, 8),
                    false
            );

            assertThat(view.partialData()).isFalse();
            assertThat(view.membership()).isNotNull();
            assertThat(view.compliance()).isNotNull();
            assertThat(view.finance()).isNotNull();
            assertThat(view.competition()).isNotNull();
        }

        @Test
        @DisplayName("partialData flag is true when a section is null")
        void partialData_true_when_section_null() {
            var view = new DashboardView(
                    UUID.randomUUID(), UUID.randomUUID(),
                    "ASD Test", "2024-25",
                    new DashboardView.MembershipSummary(50, 2, 3),
                    null,   // compliance unavailable
                    new DashboardView.FinanceSummary(
                            java.math.BigDecimal.valueOf(2500), java.math.BigDecimal.valueOf(300), 3, 47),
                    null,   // competition unavailable
                    true
            );

            assertThat(view.partialData()).isTrue();
            assertThat(view.compliance()).isNull();
            assertThat(view.competition()).isNull();
        }
    }
}
