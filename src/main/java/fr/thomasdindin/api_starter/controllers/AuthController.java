package fr.thomasdindin.api_starter.controllers;

import fr.thomasdindin.api_starter.dto.AuthResponseDTO;
import fr.thomasdindin.api_starter.dto.LoginRequestDTO;
import fr.thomasdindin.api_starter.entities.Utilisateur;
import fr.thomasdindin.api_starter.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private UtilisateurService utilisateurService;

    public AuthController(@Autowired UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            AuthResponseDTO dto = utilisateurService.authenticate(loginRequestDTO.getEmail(), loginRequestDTO.getPassword());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Utilisateur utilisateur) {
        try {
            utilisateurService.registerUtilisateur(utilisateur);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
