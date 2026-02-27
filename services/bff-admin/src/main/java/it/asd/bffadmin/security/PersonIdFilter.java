package it.asd.bffadmin.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Extracts person_id from the authenticated JWT and sets it as
 * a request attribute so controllers don't parse the JWT directly.
 *
 * JWT claim name: "person_id" (set by Identity service when issuing tokens)
 */
@Component
public class PersonIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            var personIdStr = jwt.getClaimAsString("person_id");
            if (personIdStr != null) {
                try {
                    request.setAttribute("personId", UUID.fromString(personIdStr));
                } catch (IllegalArgumentException ignored) {
                    // malformed claim — leave attribute unset, controller will 400
                }
            }
        }

        chain.doFilter(request, response);
    }
}
