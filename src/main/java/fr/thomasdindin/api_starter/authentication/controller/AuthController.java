package fr.thomasdindin.api_starter.authentication.controller;

import fr.thomasdindin.api_starter.authentication.dto.LoginRequestDTO;
import fr.thomasdindin.api_starter.authentication.errors.AccountBlockedException;
import fr.thomasdindin.api_starter.authentication.errors.AuthenticationException;
import fr.thomasdindin.api_starter.authentication.errors.EmailNotVerfiedException;
import fr.thomasdindin.api_starter.authentication.errors.NoMatchException;
import fr.thomasdindin.api_starter.authentication.service.PasswordResetService;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import fr.thomasdindin.api_starter.authentication.service.AuthenticationService;
import fr.thomasdindin.api_starter.services.VerificationEmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final VerificationEmailService verificationEmailService;
    private final PasswordResetService passwordResetService;

    public AuthController(@Autowired AuthenticationService authenticationService,
                          @Autowired VerificationEmailService verificationEmailService,
                          @Autowired PasswordResetService passwordResetService) {
        this.authenticationService = authenticationService;
        this.verificationEmailService = verificationEmailService;
        this.passwordResetService = passwordResetService;
    }

    /**
     * A la connexion, on renvoie un token JWT, ainsi qu'un refresh token.
     * Le token JWT est valide 15 minutes, et le refresh token 7 jours.
     * @param loginRequestDTO contiens l'email et le mot de passe
     * @param request la requête HTTP
     * @return un objet contenant le token JWT et le refresh token
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO, HttpServletRequest request) {
        try {
            Map<String, String> tokens = authenticationService.authenticate(loginRequestDTO.getEmail(), loginRequestDTO.getPassword(), request);
            return ResponseEntity.ok(tokens);
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
    public ResponseEntity<UUID> register(@Valid @RequestBody Utilisateur utilisateur, HttpServletRequest request) {
        try {
            UUID id = authenticationService.registerUtilisateur(utilisateur, request).getId();
            verificationEmailService.generateAndSendVerificationEmail(utilisateur);
            return ResponseEntity.ok(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<String> refreshToken(@RequestParam("refreshToken") String refreshToken) {
        try {
            String token = authenticationService.refreshToken(refreshToken);
            return ResponseEntity.ok(token);
        } catch (NoMatchException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@PathParam("uuid") UUID uuid, HttpServletRequest request) {
        try {
            authenticationService.verifyEmail(uuid, request);
            return ResponseEntity.ok().build();
        } catch (NoMatchException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // Endpoint pour demander une réinitialisation de mot de passe
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Utilisateur utilisateur) {
        // Ici, vous devriez chercher l'utilisateur par email, par exemple
        // (Supposons que l'objet utilisateur contient au moins le champ email)

        // Si l'utilisateur existe, déclencher l'envoi de l'email
        passwordResetService.generateAndSendPasswordReset(utilisateur);

        return ResponseEntity.ok("Un email pour réinitialiser votre mot de passe a été envoyé.");
    }
}
