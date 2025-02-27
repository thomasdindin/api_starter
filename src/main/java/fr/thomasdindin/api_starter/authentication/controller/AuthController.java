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
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO,
                                                     HttpServletRequest request,
                                                     HttpServletResponse response) {
        // 1. Authentification (peut lever AuthenticationException, EmailNotVerfiedException, etc.)
        Map<String, String> tokens = authenticationService.login(
                loginRequestDTO.getEmail(),
                loginRequestDTO.getPassword(),
                request
        );

        // 2. On récupère les tokens et on le met en cookie HttpOnly
        String refreshToken = tokens.get("refreshToken");
        if (refreshToken != null) {

            response.addCookie(createCookie("refreshToken", refreshToken, 7 * 24 * 60 * 60));
        }

        String accessToken = tokens.get("accessToken");
        response.addCookie(createCookie("accessToken", accessToken, 15 * 60));

        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequestDto registerRequestDTO,
                                         HttpServletRequest request) {
        // Appel du service
        Utilisateur newUser = authenticationService.register(registerRequestDTO, request);

        // Générez et envoyez l'email de vérification
        verificationEmailService.generateAndSendVerificationEmail(newUser);

        return ResponseEntity.ok().build();
    }


    /**
     * Rafraîchit le token à partir du cookie "refreshToken"
     */
    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request,
                                                            HttpServletResponse response) {
        // Récupération du cookie refreshToken
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    String refreshToken = cookie.getValue();
                    String newToken = authenticationService.refreshToken(refreshToken, request);

                    response.addCookie(createCookie("accessToken", newToken, 15 * 60));

                    return ResponseEntity.ok().build();
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
    public ResponseEntity<String> forgotPassword(@RequestBody String email) {
        // Gère la génération et l'envoi du mail
        passwordResetService.generateAndSendPasswordReset(email);
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

    /**
     * Crée un cookie HttpOnly
     * @param name Le nom du cookie
     * @param value La valeur du cookie
     * @param maxAge La durée de vie du cookie en secondes
     * @return Le cookie créé
     */
    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);         // En prod => true + HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}
