package fr.thomasdindin.api_starter.emails;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailListener {

    @Autowired
    private JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void processVerificationEmail(EmailMessage message) {
        // Construire l'email
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(message.getRecipient());
        email.setSubject("Vérification de votre adresse e-mail");

        // Exemple : envoi du code et d'un lien de vérification
        String verificationLink = "http://localhost:8080/api/auth/verify?code=" + message.getCode();
        String content = "Votre code de vérification est : " + message.getCode() + "\n\n" +
                "Vous pouvez également vérifier votre adresse e-mail en cliquant sur le lien suivant :\n" +
                verificationLink + "\n\n" +
                "Ce code/lien expirera dans 24 heures.";

        email.setText(content);

        // Envoi de l'email
        mailSender.send(email);
    }
}
