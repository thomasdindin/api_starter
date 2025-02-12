package fr.thomasdindin.api_starter.authentication.controller;

import fr.thomasdindin.api_starter.authentication.dto.LoginRequestDTO;
import fr.thomasdindin.api_starter.authentication.dto.RegisterRequestDto;
import fr.thomasdindin.api_starter.authentication.service.AuthenticationService;
import fr.thomasdindin.api_starter.authentication.service.PasswordResetService;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import fr.thomasdindin.api_starter.services.VerificationEmailService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
     * À la connexion, on renvoie un token JWT, ainsi qu'un refresh token.
     * Le token JWT est valide 15 minutes, et le refresh token 7 jours.
     * @param loginRequestDTO contient l'email et le mot de passe
     * @param request la requête HTTP
     * @return un objet contenant l'accessToken (et pas le refresh token)
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        // 1. Authentification (peut lever AuthenticationException, EmailNotVerfiedException, etc.)
        Map<String, String> tokens = authenticationService.authenticate(
                loginRequestDTO.getEmail(),
                loginRequestDTO.getPassword(),
                request
        );

        // 2. On récupère le refresh token et on le met en cookie HttpOnly
        String refreshToken = tokens.get("refreshToken");
        if (refreshToken != null) {
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);         // En prod => true + HTTPS
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(refreshCookie);
        }

        // 3. On retire le refreshToken de la réponse
        tokens.remove("refreshToken");

        // 4. On renvoie juste l'accessToken
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDto registerRequestDTO,
                                         HttpServletRequest request) {
        // Appel du service
        Utilisateur newUser = authenticationService.registerUtilisateur(registerRequestDTO, request);

        // Générez et envoyez l'email de vérification
        verificationEmailService.generateAndSendVerificationEmail(newUser);

        return ResponseEntity.ok().build();
    }


    /**
     * Rafraîchit le token (en POST) à partir du cookie "refreshToken"
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshToken(HttpServletRequest request,
                                                            HttpServletResponse response) {
        // Récupération du cookie refreshToken
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    String refreshToken = cookie.getValue();
                    String newToken = authenticationService.refreshToken(refreshToken);

                    Map<String, String> result = new HashMap<>();
                    result.put("accessToken", newToken);
                    return ResponseEntity.ok(result);
                }
            }
        }
        // Si on ne trouve pas le cookie, ou s’il est invalide => 401 (levé par l'extraction ou direct)
        return ResponseEntity.status(401).build();
    }

    /**
     * Envoie un mail pour réinitialiser le mot de passe
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Utilisateur utilisateur) {
        // Gère la génération et l'envoi du mail
        passwordResetService.generateAndSendPasswordReset(utilisateur);
        return ResponseEntity.ok("Un email pour réinitialiser votre mot de passe a été envoyé.");
    }

    /**
     * Vérifie un email à partir d'un code (GET param)
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam("code") String token) {
        // Peut lever NoMatchException
        verificationEmailService.verifyCode(token);
        return ResponseEntity.ok("Votre email a été vérifié avec succès.");
    }
}
