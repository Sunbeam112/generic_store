package ua.sunbeam.genericstore.service.EmailService;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ua.sunbeam.genericstore.error.EmailFailureException;
import ua.sunbeam.genericstore.model.VerificationToken;

@Service
public class EmailVerificationService extends BasicMailService {
    private static final String url = "localhost:3000/verify";
    private static final String greeting = "Hello! \nTo confirm the email, please follow the link:";

    public EmailVerificationService(JavaMailSender mailSender) {
        super(mailSender);
    }


    public void sendEmailConformationMessage(VerificationToken token) throws EmailFailureException {
        SimpleMailMessage message = createSimpleMailMessage();
        message.setTo(token.getLocalUser().getEmail());
        message.setSubject("Please confirm your email");
        message.setText(String.format("<h1>Test</h1><p>smaller text</p>\n%s\n%s?token=%s", greeting, url, token.getToken()));
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new EmailFailureException();
        }
    }

}
