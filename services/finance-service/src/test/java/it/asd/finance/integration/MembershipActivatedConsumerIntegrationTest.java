package it.asd.finance.integration;

import it.asd.events.EventEnvelope;
import it.asd.events.KafkaTopics;
import it.asd.finance.shared.TestFixtures;
import it.asd.finance.shared.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@DisplayName("MembershipActivatedConsumer — integration")
@Tag("integration")
class MembershipActivatedConsumerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KafkaTemplate<String, EventEnvelope> kafkaTemplate;

    @Autowired
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository.deleteAll();
    }

    @Test
    @DisplayName("creates a PENDING payment in DB when MembershipActivated event is received")
    void createsPaymentOnMembershipActivated() {
        UUID membershipId = UUID.randomUUID();
        var evt = TestFixtures.membershipActivatedEventWith(membershipId);
        var envelope = TestFixtures.envelopeOf(evt);

        kafkaTemplate.send(KafkaTopics.MEMBERSHIP_ACTIVATED, membershipId.toString(), envelope);

        await().atMost(10, SECONDS).untilAsserted(() ->
                assertThat(paymentRepository.existsByTriggerEventId(membershipId)).isTrue());

        var payments = paymentRepository.findByPersonIdAndAsdId(
                TestFixtures.PERSON_ID, TestFixtures.ASD_ID);
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getTriggerEventId()).isEqualTo(membershipId);
        assertThat(payments.get(0).getTriggerType()).isEqualTo("membership.activated");
    }

    @Test
    @DisplayName("processes each membershipId only once — idempotency")
    void processesEachMembershipOnlyOnce() {
        UUID membershipId = UUID.randomUUID();
        var evt = TestFixtures.membershipActivatedEventWith(membershipId);
        var envelope = TestFixtures.envelopeOf(evt);

        // Publish the same event twice
        kafkaTemplate.send(KafkaTopics.MEMBERSHIP_ACTIVATED, membershipId.toString(), envelope);
        kafkaTemplate.send(KafkaTopics.MEMBERSHIP_ACTIVATED, membershipId.toString(), envelope);

        await().atMost(10, SECONDS).untilAsserted(() ->
                assertThat(paymentRepository.existsByTriggerEventId(membershipId)).isTrue());

        // Exactly one payment must exist regardless of duplicate event delivery
        var payments = paymentRepository.findByPersonIdAndAsdId(
                TestFixtures.PERSON_ID, TestFixtures.ASD_ID);
        assertThat(payments).hasSize(1);
    }
}
