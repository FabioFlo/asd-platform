package it.asd.bffadmin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * All downstream WebClient beans.
 *
 * Each client targets a single core service or satellite.
 * BFF Admin never calls another BFF — only core services and satellites.
 *
 * Naming convention: {serviceName}WebClient
 */
@Configuration
public class WebClientConfig {

    // ── Core services ──────────────────────────────────────────────────────

    @Bean
    public WebClient registryWebClient(
            WebClient.Builder builder,
            @Value("${services.registry.base-url}") String url) {
        return buildClient(builder, url);
    }

    @Bean
    public WebClient identityWebClient(
            WebClient.Builder builder,
            @Value("${services.identity.base-url}") String url) {
        return buildClient(builder, url);
    }

    @Bean
    public WebClient membershipWebClient(
            WebClient.Builder builder,
            @Value("${services.membership.base-url}") String url) {
        return buildClient(builder, url);
    }

    @Bean
    public WebClient schedulingWebClient(
            WebClient.Builder builder,
            @Value("${services.scheduling.base-url}") String url) {
        return buildClient(builder, url);
    }

    @Bean
    public WebClient competitionWebClient(
            WebClient.Builder builder,
            @Value("${services.competition.base-url}") String url) {
        return buildClient(builder, url);
    }

    @Bean
    public WebClient complianceWebClient(
            WebClient.Builder builder,
            @Value("${services.compliance.base-url}") String url) {
        return buildClient(builder, url);
    }

    @Bean
    public WebClient financeWebClient(
            WebClient.Builder builder,
            @Value("${services.finance.base-url}") String url) {
        return buildClient(builder, url);
    }

    // ── Sport satellites ───────────────────────────────────────────────────

    /**
     * Builds one WebClient per registered satellite at startup.
     * Keyed by disciplina (e.g. "scacchi", "padel", "calcio").
     *
     * Inject this map wherever satellite calls are needed.
     * Never inject a sport-specific WebClient directly.
     */
    @Bean
    public java.util.Map<String, WebClient> satelliteClients(
            WebClient.Builder builder,
            SatelliteRegistry registry) {

        return registry.satellites().stream()
                .collect(java.util.stream.Collectors.toUnmodifiableMap(
                        SatelliteRegistry.SatelliteDefinition::disciplina,
                        sat -> buildClient(builder, sat.baseUrl())
                ));
    }

    // ── Private helper ─────────────────────────────────────────────────────
    // Resilience4j circuit-breaker defaults are configured via application.yml
    // (resilience4j.circuitbreaker.configs.default / resilience4j.timelimiter.configs.default)
    // and auto-applied by resilience4j-spring-boot3.

    private WebClient buildClient(WebClient.Builder builder, String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(2 * 1024 * 1024)) // 2 MB
                .build();
    }
}
