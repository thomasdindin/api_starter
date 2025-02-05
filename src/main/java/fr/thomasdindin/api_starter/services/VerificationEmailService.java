package fr.thomasdindin.api_starter.services;

import fr.thomasdindin.api_starter.emails.EmailMessage;
import fr.thomasdindin.api_starter.emails.RabbitMQConfig;
import fr.thomasdindin.api_starter.entities.utilisateur.Utilisateur;
import fr.thomasdindin.api_starter.entities.VerificationEmail;
import fr.thomasdindin.api_starter.repositories.UtilisateurRepository;
import fr.thomasdindin.api_starter.repositories.VerificationEmailRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.Duration;
import java.util.Random;

@Service
public class VerificationEmailService {

    private final UtilisateurRepository utilisateurRepository;
    private final VerificationEmailRepository verificationEmailRepository;
    private final RabbitTemplate rabbitTemplate;
    private final Random random = new Random();

    public VerificationEmailService(VerificationEmailRepository verificationEmailRepository,
                                    RabbitTemplate rabbitTemplate, @Autowired UtilisateurRepository utilisateurRepository) {
        this.verificationEmailRepository = verificationEmailRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Génère un code de vérification, l’enregistre en base et envoie un email asynchrone.
     *
     * @param utilisateur L’utilisateur auquel envoyer le code
     */
    public void generateAndSendVerificationEmail(Utilisateur utilisateur) {
        // Génère un code numérique à 6 chiffres, par exemple "035214"
        String code = String.format("%06d", random.nextInt(1_000_000));

        // Crée l'entité de vérification
        VerificationEmail verificationEmail = new VerificationEmail();
        verificationEmail.setUtilisateur(utilisateur);
        verificationEmail.setCode(code);
        // Date d'expiration : 24 heures à partir de maintenant
        verificationEmail.setDateExpiration(Instant.now().plus(Duration.ofHours(24)));
        verificationEmail.setVerifie(false);

        // Sauvegarde l'entité en base
        verificationEmailRepository.save(verificationEmail);

        // Prépare le message pour RabbitMQ
        EmailMessage message = new EmailMessage();
        // On suppose que l’entité Utilisateur possède bien un champ email accessible via getEmail()
        message.setRecipient(utilisateur.getEmail());
        message.setCode(code);

        // Publie le message dans la file (le message sera traité par le listener d’email)
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EMAIL_EXCHANGE,
                RabbitMQConfig.EMAIL_ROUTING_KEY,
                message
        );
    }

    public boolean verifyCode(String code) {
        // Recherche l'entité de vérification
        VerificationEmail verificationEmail = verificationEmailRepository.findByCode(code).orElseThrow();

        // Rechercher l'utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(verificationEmail.getUtilisateur().getId()).orElseThrow();

        // Vérifie que le code est correct
        if (!verificationEmail.getCode().equals(code)) {
            return false;
        }

        // Vérifie que le code n'a pas expiré
        if (verificationEmail.getDateExpiration().isBefore(Instant.now())) {
            return false;
        }

        // Marque l'entité de vérification comme vérifiée
        verificationEmail.setVerifie(true);
        verificationEmailRepository.save(verificationEmail);

        // Active le compte de l'utilisateur
        utilisateur.setCompteActive(true);
        utilisateurRepository.save(utilisateur);

        return true;
    }
}
