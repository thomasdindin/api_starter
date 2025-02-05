package fr.thomasdindin.api_starter.controllers;

import fr.thomasdindin.api_starter.authentication.utils.JwtUtils;
import fr.thomasdindin.api_starter.dto.UtilisateurDto;
import fr.thomasdindin.api_starter.services.UtilisateurService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;
    private final JwtUtils jwtUtils;

    public UtilisateurController(@Autowired UtilisateurService utilisateurService, @Autowired JwtUtils jwtUtils) {
        this.utilisateurService = utilisateurService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/me")
    public ResponseEntity<UtilisateurDto> getMe(HttpServletRequest request) {
        try {
            final String authHeader = request.getHeader("Authorization");
            String jwt = authHeader.substring(7);
            UUID userId = UUID.fromString(jwtUtils.extractSubject(jwt));
            return ResponseEntity.ok(utilisateurService.findById(userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
