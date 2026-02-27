package it.asd.compliance.features.checkeligibility;

import it.asd.compliance.shared.TestFixtures;
import it.asd.compliance.shared.repository.DocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckEligibilityHandler")
@Tag("unit")
class CheckEligibilityHandlerTest {

    @Mock
    private DocumentRepository repository;

    @InjectMocks
    private CheckEligibilityHandler handler;

    private final UUID personId = TestFixtures.PERSON_ID;
    private final UUID asdId = TestFixtures.ASD_ID;

    @Nested
    @DisplayName("when all required documents are valid")
    class WhenEligible {

        @Test
        @DisplayName("returns Eligible")
        void returnsEligible() {
            // All recreational required docs present and valid
            when(repository.findActiveByPersonAsdAndType(eq(personId), eq(asdId), any()))
                    .thenReturn(Optional.of(TestFixtures.validDocument()));

            var result = handler.handle(new CheckEligibilityQuery(personId, asdId, false));

            assertThat(result).isInstanceOf(EligibilityResult.Eligible.class);
        }
    }

    @Nested
    @DisplayName("when a required document is missing")
    class WhenIneligible {

        @Test
        @DisplayName("returns Ineligible with blocking documents listed")
        void returnsIneligible() {
            // No documents found for any type
            when(repository.findActiveByPersonAsdAndType(any(), any(), any()))
                    .thenReturn(Optional.empty());

            var result = handler.handle(new CheckEligibilityQuery(personId, asdId, false));

            assertThat(result).isInstanceOf(EligibilityResult.Ineligible.class);
            var ineligible = (EligibilityResult.Ineligible) result;
            assertThat(ineligible.blockingDocuments()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("when a document is expiring soon")
    class WhenExpiringSoon {

        @Test
        @DisplayName("returns ExpiringSoon with warning")
        void returnsExpiringSoon() {
            var expiringSoon = TestFixtures.expiringSoonDocument(UUID.randomUUID());
            when(repository.findActiveByPersonAsdAndType(any(), any(), any()))
                    .thenReturn(Optional.of(expiringSoon));

            var result = handler.handle(new CheckEligibilityQuery(personId, asdId, false));

            assertThat(result).isInstanceOf(EligibilityResult.ExpiringSoon.class);
            var warning = (EligibilityResult.ExpiringSoon) result;
            assertThat(warning.expiringDocuments()).isNotEmpty();
        }
    }
}
