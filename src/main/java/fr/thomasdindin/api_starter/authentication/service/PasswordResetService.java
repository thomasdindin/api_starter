package fr.thomasdindin.api_starter.authentication.service;

import fr.thomasdindin.api_starter.authentication.errors.AuthenticationException;
import fr.thomasdindin.api_starter.authentication.errors.NoMatchException;
import fr.thomasdindin.api_starter.dto.UtilisateurDto;
import fr.thomasdindin.api_starter.emails.EmailMessage;
import fr.thomasdindin.api_starter.emails.RabbitMQConfig;
import fr.thomasdindin.api_starter.entities.PasswordReset;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import fr.thomasdindin.api_starter.mappers.UtilisateurMapper;
import fr.thomasdindin.api_starter.repositories.PasswordResetRepository;
import fr.thomasdindin.api_starter.repositories.UtilisateurRepository;
import fr.thomasdindin.api_starter.services.UtilisateurService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final RabbitTemplate rabbitTemplate;

    public PasswordResetService(PasswordResetRepository passwordResetRepository,
                                RabbitTemplate rabbitTemplate,
                                UtilisateurRepository utilisateurRepository) {
        this.passwordResetRepository = passwordResetRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.utilisateurRepository = utilisateurRepository;
    }

    /**
     * Génère un token pour la réinitialisation du mot de passe, l'enregistre en base et envoie l'email.
     *
     * @param email L'email de l'utilisateur pour lequel on veut réinitialiser le mot de passe
     */
    public void generateAndSendPasswordReset(String email) {
        // On récupère l'utilisateur correspondant à l'email
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email).orElseThrow(
                () -> new NoMatchException("Utilisateur non trouvé")
        );

        Optional<PasswordReset> existingPasswordReset = passwordResetRepository.findByUtilisateurId(utilisateur.getId());

        if (existingPasswordReset.isPresent() && !existingPasswordReset.get().getUsed()) {
            throw new AuthenticationException("Un email a déjà été envoyé pour réinitialiser le mot de passe.");
        }

        // Génération d'un token de 64 caractères en concaténant deux UUID (32 caractères chacun)
        String token = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");

        // Création de l'entité PasswordReset
        PasswordReset passwordReset = new PasswordReset();
        passwordReset.setUtilisateur(utilisateur);
        passwordReset.setToken(token);
        passwordReset.setDateExpiration(Instant.now().plus(Duration.ofHours(1)));
        passwordReset.setUsed(false);

        passwordResetRepository.save(passwordReset);

        // Préparation du message RabbitMQ
        EmailMessage message = new EmailMessage();
        message.setRecipient(utilisateur.getEmail());
        message.setCode(token);
        // Envoi dans la file dédiée aux réinitialisations
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.PASSWORD_RESET_ROUTING_KEY,
                message
        );
    }
}
