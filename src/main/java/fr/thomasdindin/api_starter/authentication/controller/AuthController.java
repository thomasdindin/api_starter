package fr.thomasdindin.api_starter.authentication.controller;

import fr.thomasdindin.api_starter.audit.AuditAction;
import fr.thomasdindin.api_starter.audit.service.AuditLogService;
import fr.thomasdindin.api_starter.authentication.dto.AuthResponseDTO;
import fr.thomasdindin.api_starter.authentication.dto.LoginRequestDTO;
import fr.thomasdindin.api_starter.authentication.errors.AccountBlockedException;
import fr.thomasdindin.api_starter.authentication.errors.AuthenticationException;
import fr.thomasdindin.api_starter.authentication.errors.EmailNotVerfiedException;
import fr.thomasdindin.api_starter.authentication.errors.NoMatchException;
import fr.thomasdindin.api_starter.entities.Utilisateur;
import fr.thomasdindin.api_starter.authentication.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private AuthenticationService authenticationService;

    public AuthController(@Autowired AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO, HttpServletRequest request) {
        try {
            AuthResponseDTO dto = authenticationService.authenticate(loginRequestDTO.getEmail(), loginRequestDTO.getPassword(), request);
            return ResponseEntity.ok(dto);
        } catch (AuthenticationException e) {
            // Mot de passe incorrect ou utilisateur non trouvé : 401
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (EmailNotVerfiedException e) {
            // L'email n'a pas été vérifié : 412
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
        } catch (AccountBlockedException e) {
            // Trop de tentatives de connexion : 429
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody Utilisateur utilisateur, HttpServletRequest request) {
        try {
            authenticationService.registerUtilisateur(utilisateur, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
