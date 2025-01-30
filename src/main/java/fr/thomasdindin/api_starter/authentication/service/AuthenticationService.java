package fr.thomasdindin.api_starter.authentication.service;

import fr.thomasdindin.api_starter.audit.AuditAction;
import fr.thomasdindin.api_starter.audit.service.AuditLogService;
import fr.thomasdindin.api_starter.authentication.dto.AuthResponseDTO;
import fr.thomasdindin.api_starter.authentication.errors.AccountBlockedException;
import fr.thomasdindin.api_starter.authentication.errors.AuthenticationException;
import fr.thomasdindin.api_starter.authentication.errors.EmailNotVerfiedException;
import fr.thomasdindin.api_starter.authentication.errors.NoMatchException;
import fr.thomasdindin.api_starter.authentication.validators.PasswordValidator;
import fr.thomasdindin.api_starter.authentication.validators.ValidPassword;
import fr.thomasdindin.api_starter.entities.Utilisateur;
import fr.thomasdindin.api_starter.repositories.UtilisateurRepository;
import fr.thomasdindin.api_starter.authentication.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthenticationService {

    private final UtilisateurRepository utilisateurRepository;
    private final AuditLogService auditLogService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    private final short maxLoginAttempts = 3;


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

    public AuthResponseDTO authenticate(String email, String motDePasse, HttpServletRequest request) {

        Utilisateur utilisateur = searchForEmail(email, request);

        // Conditions d'accès
        if (Boolean.TRUE.equals(utilisateur.getCompteBloque())) {
            auditLogService.log(AuditAction.BLOCKED_ACCOUNT, utilisateur, request.getRemoteAddr());
            throw new AccountBlockedException("Votre compte est temporairement bloqué suite à des tentatives échouées.");
        } else if (!passwordEncoder.matches(motDePasse, utilisateur.getMotDePasse())) {
            logginError(request, utilisateur);
            throw new AuthenticationException("Mot de passe incorrect");
        } else if (!utilisateur.getCompteActive()) {
            auditLogService.log(AuditAction.FAILED_LOGIN, utilisateur, request.getRemoteAddr());
            throw new EmailNotVerfiedException("Votre compte n'est pas activé");
        }

        // Réinitialiser le compteur de tentatives après un login réussi
        utilisateur.setTentativesConnexion((short) 0);
        utilisateurRepository.save(utilisateur);

        // Générer un JWT
        String token = jwtUtils.generateToken(utilisateur);

        // Générer le log de connexion réussie
        auditLogService.log(AuditAction.SUCCESSFUL_LOGIN, utilisateur, request.getRemoteAddr());

        // Renvoyer les données sous forme de DTO
        return new AuthResponseDTO(
                utilisateur.getId().toString(),
                utilisateur.getEmail(),
                token
        );
    }

    public Utilisateur verifyEmail(UUID uuid, HttpServletRequest request) {
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(uuid);

        if (utilisateurOpt.isEmpty()) {
            auditLogService.log(AuditAction.FAILED_EMAIL_VERIFICATION, null, request.getRemoteAddr());
            throw new NoMatchException("Utilisateur non trouvé");
        } else if (utilisateurOpt.get().getCompteActive()) {
            auditLogService.log(AuditAction.FAILED_EMAIL_VERIFICATION, utilisateurOpt.get(), request.getRemoteAddr());
            throw new UnsupportedOperationException("Votre compte est déjà activé");
        }

        Utilisateur utilisateur = utilisateurOpt.get();
        utilisateur.setCompteActive(true);
        utilisateurRepository.save(utilisateur);

        auditLogService.log(AuditAction.SUCCESSFUL_EMAIL_VERIFICATION, utilisateur, request.getRemoteAddr());
        return utilisateur;
    }

    public void logout(HttpServletRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private void logginError(HttpServletRequest request, Utilisateur utilisateur) {
        // Incrémenter le compteur de tentatives échouées
        short tentativeActuelle = utilisateur.getTentativesConnexion() == null ? 0 : utilisateur.getTentativesConnexion();
        utilisateur.setTentativesConnexion((short) (tentativeActuelle + 1));

        // Bloquer le compte si le nombre de tentatives dépasse le seuil
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
            throw new AuthenticationException("Utilisateur non trouvé");
        }
        return utilisateurOpt.get();
    }

}
