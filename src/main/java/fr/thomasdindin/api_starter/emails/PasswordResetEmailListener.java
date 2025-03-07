package fr.thomasdindin.api_starter.emails;


import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class PasswordResetEmailListener {
    private final JavaMailSender mailSender;

    public PasswordResetEmailListener(@Autowired JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @RabbitListener(queues = RabbitMQConfig.PASSWORD_RESET_QUEUE)
    public void processPasswordResetEmail(EmailMessage message) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(message.getRecipient());
        email.setSubject("Réinitialisation de votre mot de passe");

        // Par exemple, créer un lien pointant vers votre endpoint de réinitialisation
        String resetLink = "http://localhost:8080/api/auth/reset-password?token=" + message.getCode();
        String content = "Pour réinitialiser votre mot de passe, veuillez cliquer sur le lien suivant :\n" +
                resetLink + "\n\nCe lien expirera dans 1 heure.";
        email.setText(content);

        mailSender.send(email);
    }
}
