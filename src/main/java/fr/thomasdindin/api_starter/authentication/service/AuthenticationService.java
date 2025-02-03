package fr.thomasdindin.api_starter.authentication.service;

import fr.thomasdindin.api_starter.audit.AuditAction;
import fr.thomasdindin.api_starter.audit.service.AuditLogService;
import fr.thomasdindin.api_starter.authentication.errors.AccountBlockedException;
import fr.thomasdindin.api_starter.authentication.errors.AuthenticationException;
import fr.thomasdindin.api_starter.authentication.errors.EmailNotVerfiedException;
import fr.thomasdindin.api_starter.authentication.errors.NoMatchException;
import fr.thomasdindin.api_starter.entities.Utilisateur;
import fr.thomasdindin.api_starter.repositories.UtilisateurRepository;
import fr.thomasdindin.api_starter.authentication.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {

    public static final String UTILISATEUR_NON_TROUVE = "Utilisateur non trouvé";
    private final UtilisateurRepository utilisateurRepository;
    private final AuditLogService auditLogService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;


    public AuthenticationService(@Autowired UtilisateurRepository utilisateurRepository, @Autowired JwtUtils jwtUtils, @Autowired AuditLogService auditLogService) {
        this.utilisateurRepository = utilisateurRepository;
        this.auditLogService = auditLogService;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.jwtUtils = jwtUtils;
    }

    /**
     * Inscription d'un nouvel utilisateur
     */
    public Utilisateur registerUtilisateur(Utilisateur utilisateur, HttpServletRequest request) {
        // Vérifier si l'utilisateur existe déjà
        Optional<Utilisateur> existingUser = utilisateurRepository.findByEmail(utilisateur.getEmail());
        if (existingUser.isPresent()) {
            auditLogService.log(AuditAction.FAILED_REGISTRATION, existingUser.get(), request.getRemoteAddr());
            throw new AuthenticationException("Un utilisateur avec cet e-mail existe déjà.");
        }

        // Hacher le mot de passe
        String encodedPassword = passwordEncoder.encode(utilisateur.getMotDePasse());
        utilisateur.setMotDePasse(encodedPassword);

        Utilisateur savedUtilisateur = utilisateurRepository.save(utilisateur);
        auditLogService.log(AuditAction.SUCCESSFUL_REGISTRATION, savedUtilisateur, request.getRemoteAddr());
        return savedUtilisateur;
    }

    public Map<String, String> authenticate(String email, String motDePasse, HttpServletRequest request) {

        Utilisateur utilisateur = searchForEmail(email, request);

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

        // Renvoyer les tokens d'authentification
        return jwtUtils.generateTokens(utilisateur);
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

    public String refreshToken(String refreshToken) {
        String userId = jwtUtils.extractSubject(refreshToken);
        Utilisateur utilisateur = utilisateurRepository.findById(UUID.fromString(userId)).orElseThrow(() -> new NoMatchException(UTILISATEUR_NON_TROUVE));

        return jwtUtils.generateToken(utilisateur);
    }

}
