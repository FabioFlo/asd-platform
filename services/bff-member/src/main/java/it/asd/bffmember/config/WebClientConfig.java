package it.asd.bffmember.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient beans for BFF Member.
 * Same pattern as BFF Admin — one bean per downstream service.
 */
@Configuration
public class WebClientConfig {

    @Bean public WebClient membershipWebClient(WebClient.Builder b, @Value("${services.membership.base-url}") String url) { return build(b, url); }
    @Bean public WebClient complianceWebClient(WebClient.Builder b, @Value("${services.compliance.base-url}") String url) { return build(b, url); }
    @Bean public WebClient financeWebClient(WebClient.Builder b,    @Value("${services.finance.base-url}")    String url) { return build(b, url); }
    @Bean public WebClient competitionWebClient(WebClient.Builder b,@Value("${services.competition.base-url}") String url){ return build(b, url); }

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
                        sat -> build(builder, sat.baseUrl())
                ));
    }

    private WebClient build(WebClient.Builder builder, String baseUrl) {
        return builder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT,       MediaType.APPLICATION_JSON_VALUE)
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }
}
