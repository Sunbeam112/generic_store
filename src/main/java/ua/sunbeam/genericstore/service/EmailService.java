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
    private final String frontendURL = "http://localhost:8080/auth/v1/verify";
    private final String greeting = "Hello! \nTo confirm the email, please follow the link:";


    private final JavaMailSender mailSender;

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
        message.setText(String.format("%s\n%s?token=%s", greeting, frontendURL, token.getToken()));
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new EmailFailureException();
        }

    }
}
