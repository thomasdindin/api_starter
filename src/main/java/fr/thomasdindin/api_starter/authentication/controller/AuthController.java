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
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        try {
            // On récupère la map de tokens { "accessToken": "...", "refreshToken": "..." }
            Map<String, String> tokens = authenticationService.authenticate(loginRequestDTO.getEmail(), loginRequestDTO.getPassword(), request);

            // Extraire le refresh token
            String refreshToken = tokens.get("refreshToken");

            // Créer un cookie HttpOnly
            if (refreshToken != null) {
                Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
                refreshCookie.setHttpOnly(true);        // Empêche l'accès depuis JS
                refreshCookie.setSecure(false);          // Exige HTTPS (recommandé en prod)
                refreshCookie.setPath("/");             // Chemin où le cookie est valable
                refreshCookie.setMaxAge(7 * 24 * 60 * 60); // Durée de vie, ex. 7 jours
                // refreshCookie.setDomain("votre-domaine.com"); // Optionnel, si vous voulez restreindre le domaine
                // refreshCookie.setSameSite("None"); // Ou "Lax" / "Strict", dépend de votre config
                response.addCookie(refreshCookie);
            }

            // Ne plus renvoyer le refreshToken en clair :
            tokens.remove("refreshToken");

            // On renvoie juste l'accessToken (et éventuellement d'autres infos).
            return ResponseEntity.ok(tokens);

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (EmailNotVerfiedException e) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).build();
        } catch (AccountBlockedException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
    }


    @PostMapping("/register")
    public ResponseEntity register(@Valid @RequestBody Utilisateur utilisateur, HttpServletRequest request) {
        try {
            authenticationService.registerUtilisateur(utilisateur, request);
            verificationEmailService.generateAndSendVerificationEmail(utilisateur);
            return ResponseEntity.ok().build();
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

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(HttpServletRequest request,
                                                            HttpServletResponse response) {
        // Récupération du cookie refreshToken
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    String refreshToken = cookie.getValue();
                    // Vérifier / décoder refreshToken, etc.
                    // Si valide, générer un nouvel accessToken, potentiellement un nouveau refreshToken

                    String newToken = authenticationService.refreshToken(refreshToken);

                    // Renvoyer un JSON avec le nouvel accessToken
                    Map<String, String> result = new HashMap<>();
                    result.put("accessToken", newToken);
                    return ResponseEntity.ok(result);
                }
            }
        }

        // Si on ne trouve pas le cookie refreshToken ou s’il est invalide => 401
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Utilisateur utilisateur) {
        passwordResetService.generateAndSendPasswordReset(utilisateur);

        return ResponseEntity.ok("Un email pour réinitialiser votre mot de passe a été envoyé.");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("code") String token) {
        try {
            verificationEmailService.verifyCode(token);
            return ResponseEntity.ok("Votre email a été vérifié avec succès.");
        } catch (NoMatchException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
