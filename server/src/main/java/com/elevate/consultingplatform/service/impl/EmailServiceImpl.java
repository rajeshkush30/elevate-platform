package com.elevate.consultingplatform.service.impl;

import com.elevate.consultingplatform.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.client.base-url:${app.base-url:http://localhost:3001}}")
    private String clientBaseUrl;

    @Override
    @Async
    public void sendVerificationEmail(String to, String name, String token) {
    String verificationUrl = clientBaseUrl + "/verify-email?token=" + token;
        
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("verificationUrl", verificationUrl);
        
        String content = templateEngine.process("email/verification-email", context);
        
        sendEmail(to, "Verify your email address", content);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String name, String token) {
    String resetUrl = clientBaseUrl + "/reset-password?token=" + token;
        
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("resetUrl", resetUrl);
        
        String content = templateEngine.process("email/reset-password-email", context);
        
        sendEmail(to, "Password Reset Request", content);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String name) {
        Context context = new Context();
        context.setVariable("name", name);
        
        String content = templateEngine.process("email/welcome-email", context);
        
        sendEmail(to, "Welcome to Our Platform!", content);
    }

    private void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
