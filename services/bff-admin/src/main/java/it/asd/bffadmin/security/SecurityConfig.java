package it.asd.bffadmin.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * BFF Admin security configuration.
 *
 * All requests require a valid JWT.  Roles are extracted from the "roles" claim
 * and mapped to Spring Security GrantedAuthority (prefix ROLE_).
 *
 * In the full stack the API Gateway validates the JWT signature.
 * This config adds a second validation layer — defence in depth.
 *
 * Allowed roles for this BFF: ROLE_DIRETTORE, ROLE_SEGRETARIO, ROLE_ALLENATORE
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final PersonIdFilter personIdFilter;

    public SecurityConfig(PersonIdFilter personIdFilter) {
        this.personIdFilter = personIdFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/admin/**")
                            .hasAnyRole("DIRETTORE", "SEGRETARIO", "ALLENATORE")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .addFilterAfter(personIdFilter, BearerTokenAuthenticationFilter.class)
                .build();
    }

    /**
     * Reads roles from the "roles" JWT claim (string list).
     * Spring Security will prefix each value with "ROLE_".
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        var grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return converter;
    }
}
