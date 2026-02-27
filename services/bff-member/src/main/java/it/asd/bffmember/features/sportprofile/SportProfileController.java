package it.asd.bffmember.features.sportprofile;

import it.asd.satellite.SportProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * GET /member/sport/{disciplina}/profile
 *
 * Single endpoint for all sports. The disciplina path variable routes
 * to the correct satellite via the registry.
 *
 * The Vue SPA calls this endpoint from every sport-specific page
 * (/chess, /padel, /football) with the appropriate disciplina.
 */
@RestController
@RequestMapping("/member/sport")
public class SportProfileController {

    private final SportProfileHandler handler;

    public SportProfileController(SportProfileHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/{disciplina}/profile")
    @PreAuthorize("hasAnyRole('ATLETA', 'SOCIO')")
    public ResponseEntity<SportProfile> getProfile(
            @PathVariable String disciplina,
            @RequestAttribute("personId") UUID personId) {   // injected by security filter

        var profile = handler.handle(new SportProfileQuery(personId, disciplina));
        return ResponseEntity.ok(profile);
    }
}
