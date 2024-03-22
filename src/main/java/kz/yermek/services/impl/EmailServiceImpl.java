package kz.yermek.services.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import kz.yermek.models.User;
import kz.yermek.services.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine engine;

    @Override
    public void sendConfirmationEmail(String link, User user) {
        Context context = new Context();
        context.setVariable("confirmEmailUrl", link);
        String emailBody = engine.process("confirmation_email", context);
        sendConfirm(user.getEmail(), emailBody);
    }

    public void sendConfirm(String to, String body) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setText(body, true);
            helper.setTo(to);
            helper.setSubject("Confirm email");
            helper.setFrom("shmanovermek@gmail.com");
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed with sending email");
        }
    }
}
