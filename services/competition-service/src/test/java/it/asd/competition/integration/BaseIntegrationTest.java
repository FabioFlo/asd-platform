package it.asd.competition.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"));

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.0"));

    @LocalServerPort
    protected int port;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",           postgres::getJdbcUrl);
        registry.add("spring.datasource.username",      postgres::getUsername);
        registry.add("spring.datasource.password",      postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers",  kafka::getBootstrapServers);
        // Point ComplianceClient at a non-existent URL so fail-closed behaviour is testable.
        // Individual tests that need eligible=true must @MockBean ComplianceClient.
        registry.add("services.compliance.base-url",
                () -> "http://localhost:0");
    }
}
