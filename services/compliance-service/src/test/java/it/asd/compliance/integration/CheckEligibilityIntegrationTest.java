package it.asd.compliance.integration;

import it.asd.compliance.features.checkeligibility.EligibilityResponse;
import it.asd.compliance.features.uploaddocument.UploadDocumentCommand;
import it.asd.compliance.shared.TestFixtures;
import it.asd.compliance.shared.entity.DocumentType;
import it.asd.compliance.shared.repository.DocumentRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CheckEligibility — integration")
@Tag("integration")
class CheckEligibilityIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DocumentRepository documentRepository;

    @BeforeEach
    void setUp() {
        documentRepository.deleteAll();
    }

    @Test
    @DisplayName("returns eligible=true when all required recreational documents are valid")
    void returnsEligibleWhenAllDocsPresent() {
        // Upload all recreational required docs
        uploadDocument(DocumentType.CERTIFICATO_MEDICO_NON_AGONISTICO, LocalDate.now().plusYears(1));
        uploadDocument(DocumentType.ASSICURAZIONE, LocalDate.now().plusYears(1));

        var response = checkEligibility(false);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().eligible()).isTrue();
        assertThat(response.getBody().blockingDocuments()).isEmpty();
    }

    @Test
    @DisplayName("returns eligible=false when required document is missing")
    void returnsIneligibleWhenDocMissing() {
        // No documents uploaded

        var response = checkEligibility(false);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().eligible()).isFalse();
        assertThat(response.getBody().blockingDocuments()).isNotEmpty();
    }

    @Test
    @DisplayName("returns eligible=false when document is expired")
    void returnsIneligibleWhenDocExpired() {
        uploadDocument(DocumentType.CERTIFICATO_MEDICO_NON_AGONISTICO, LocalDate.now().minusDays(1));
        uploadDocument(DocumentType.ASSICURAZIONE, LocalDate.now().minusDays(1));

        var response = checkEligibility(false);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().eligible()).isFalse();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void uploadDocument(DocumentType tipo, LocalDate scadenza) {
        var cmd = new UploadDocumentCommand(
                TestFixtures.PERSON_ID, TestFixtures.ASD_ID, tipo,
                LocalDate.now().minusDays(10), scadenza,
                null, null, null, null);
        restTemplate.postForEntity(
                "http://localhost:" + port + "/compliance/persons/" + TestFixtures.PERSON_ID + "/documents",
                cmd, Object.class);
    }

    private ResponseEntity<EligibilityResponse> checkEligibility(boolean agonistic) {
        var url = "http://localhost:" + port + "/compliance/persons/" + TestFixtures.PERSON_ID
                + "/eligibility?asdId=" + TestFixtures.ASD_ID + "&agonistic=" + agonistic;
        return restTemplate.getForEntity(url, EligibilityResponse.class);
    }
}
