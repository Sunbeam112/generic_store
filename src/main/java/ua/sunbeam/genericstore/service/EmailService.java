package ua.sunbeam.genericstore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ua.sunbeam.genericstore.error.EmailFailureException;
import ua.sunbeam.genericstore.model.VerificationToken;

@Service
public class EmailService {
    @Value("${email.from}")
    private String fromAddress;
    @Value("${app.frontend.url}")
    private String frontendURL;
    private final String template =
            String.format("\"Please follow the link: %s/auth/v1/verify?token=", frontendURL);


    private JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }


    private SimpleMailMessage createSimpleMailMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        return message;
    }

    public void sendEmailConformationMessage(VerificationToken token) throws EmailFailureException {
        SimpleMailMessage message = createSimpleMailMessage();
        message.setTo(token.getLocalUser().getEmail());
        message.setSubject("Please confirm your email");
        message.setText(template + token.getToken());
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new EmailFailureException();
        }

    }
}
