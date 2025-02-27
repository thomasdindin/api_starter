package fr.thomasdindin.api_starter.authentication.service;

import fr.thomasdindin.api_starter.audit.AuditAction;
import fr.thomasdindin.api_starter.audit.service.AuditLogService;
import fr.thomasdindin.api_starter.authentication.dto.RegisterRequestDto;
import fr.thomasdindin.api_starter.authentication.errors.*;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import fr.thomasdindin.api_starter.repositories.UtilisateurRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {

    public static final String UTILISATEUR_NON_TROUVE = "Utilisateur non trouvé";
    private final UtilisateurRepository utilisateurRepository;
    private final AuditLogService auditLogService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;


    public AuthenticationService(@Autowired UtilisateurRepository utilisateurRepository, @Autowired JwtService jwtService, @Autowired AuditLogService auditLogService, @Autowired RefreshTokenService refreshTokenService) {
        this.utilisateurRepository = utilisateurRepository;
        this.auditLogService = auditLogService;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Inscription d'un nouvel utilisateur
     */
    public Utilisateur register(RegisterRequestDto dto, HttpServletRequest request) {
        // Vérifier si l'utilisateur existe déjà
        Optional<Utilisateur> existingUser = utilisateurRepository.findByEmail(dto.getEmail());
        if (existingUser.isPresent()) {
            auditLogService.log(AuditAction.FAILED_REGISTRATION, existingUser.get(), request.getRemoteAddr());
            throw new EmailAlreadyUsedException("Un utilisateur avec cet e-mail existe déjà.");
        }

        // Construire un nouvel utilisateur
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail(dto.getEmail());
        utilisateur.setNom(dto.getNom());
        utilisateur.setPrenom(dto.getPrenom());

        // Hacher le mot de passe
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        utilisateur.setMotDePasse(encodedPassword);

        // Sauvegarde en base
        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
        auditLogService.log(AuditAction.SUCCESSFUL_REGISTRATION, savedUtilisateur, request.getRemoteAddr());

        return savedUtilisateur;
    }


    /**
     * Connexion d'un utilisateur
     * @param email L'email de l'utilisateur
     * @param motDePasse Le mot de passe de l'utilisateur
     * @param request La requête HTTP
     * @return Un objet contenant l'accessToken et le refreshToken
     */
    public Map<String, String> login(String email, String motDePasse, HttpServletRequest request) {

        Utilisateur utilisateur = searchForEmail(email, request);
        Map<String, String> tokens = new HashMap<>();

        // Conditions d'accès
        if (Boolean.TRUE.equals(utilisateur.getCompteBloque())) {
            auditLogService.log(AuditAction.BLOCKED_ACCOUNT, utilisateur, request.getRemoteAddr());
            throw new AccountBlockedException("Votre compte est temporairement bloqué suite à des tentatives échouées.");
        } else if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse())) {
            logginError(request, utilisateur);
            throw new AuthenticationException("Mot de passe incorrect");
        } else if (Boolean.FALSE.equals(utilisateur.getCompteActive())) {
            auditLogService.log(AuditAction.FAILED_LOGIN, utilisateur, request.getRemoteAddr());
            throw new EmailNotVerfiedException("Votre compte n'est pas activé");
        }

        // Réinitialiser le compteur de tentatives après un login réussi
        utilisateur.setTentativesConnexion((short) 0);
        utilisateurRepository.save(utilisateur);

        // Générer le log de connexion réussie
        auditLogService.log(AuditAction.SUCCESSFUL_LOGIN, utilisateur, request.getRemoteAddr());

        // Générer les tokens
        tokens.put("accessToken", jwtService.generateAccessToken(utilisateur));
        tokens.put("refreshToken", jwtService.generateRefreshToken(utilisateur));

        refreshTokenService.createRefreshToken(tokens.get("refreshToken"), utilisateur, request);

        // Renvoyer les tokens d'authentification
        return tokens;
    }

    public Utilisateur verifyEmail(UUID uuid, HttpServletRequest request) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(uuid);

        if (utilisateurOpt.isEmpty()) {
            auditLogService.log(AuditAction.FAILED_EMAIL_VERIFICATION, null, request.getRemoteAddr());
            throw new NoMatchException(UTILISATEUR_NON_TROUVE);
        } else if (Boolean.TRUE.equals(utilisateurOpt.get().getCompteActive())) {
            auditLogService.log(AuditAction.FAILED_EMAIL_VERIFICATION, utilisateurOpt.get(), request.getRemoteAddr());
            throw new UnsupportedOperationException("Votre compte est déjà activé");
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        utilisateur.setCompteActive(true);
        utilisateurRepository.save(utilisateur);

        auditLogService.log(AuditAction.SUCCESSFUL_EMAIL_VERIFICATION, utilisateur, request.getRemoteAddr());
        return utilisateur;
    }

    private void logginError(HttpServletRequest request, Utilisateur utilisateur) {
        // Incrémenter le compteur de tentatives échouées
        short tentativeActuelle = utilisateur.getTentativesConnexion() == null ? 0 : utilisateur.getTentativesConnexion();
        utilisateur.setTentativesConnexion((short) (tentativeActuelle + 1));

        // Bloquer le compte si le nombre de tentatives dépasse le seuil
        short maxLoginAttempts = 3;
        if (utilisateur.getTentativesConnexion() >= maxLoginAttempts) {
            utilisateur.setCompteBloque(true);
            utilisateurRepository.save(utilisateur);

            auditLogService.log(AuditAction.BLOCKED_ACCOUNT, utilisateur, request.getRemoteAddr());
            throw new AuthenticationException("Votre compte a été bloqué suite à trop de tentatives échouées.");
        }

        utilisateurRepository.save(utilisateur); // Sauvegarder les tentatives échouées
        auditLogService.log(AuditAction.FAILED_LOGIN, utilisateur, request.getRemoteAddr());
    }

    private Utilisateur searchForEmail(String email, HttpServletRequest request) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByEmail(email);

        if (utilisateurOpt.isEmpty()) {
            auditLogService.log(AuditAction.FAILED_LOGIN, null, request.getRemoteAddr());
            throw new AuthenticationException(AuthenticationService.UTILISATEUR_NON_TROUVE);
        }
        return utilisateurOpt.get();
    }

    public String refreshToken(String refreshToken, HttpServletRequest request) {
        return refreshTokenService.refreshAccessToken(refreshToken, request);
    }

}
