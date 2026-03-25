package com.hackaton.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {


        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8");
            helper.setText(body, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setFrom("no-reply@iks-gmbh.com");
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send invitation email", e);
        }

        mailSender.send(mimeMessage);
    }

    public void sendGroupInvitation(String toEmail, String groupName, String inviteToken) {
        String inviteLink = "http://localhost:5173/invite/" + inviteToken;

        String message = "Hallo,\n\n" +
                "du wurdest zur Gruppe \"" + groupName + "\" eingeladen.\n\n" +
                "Klicke auf den folgenden Link, um die Einladung anzunehmen:\n" +
                inviteLink + "\n\n" +
                "Viele Grüße,\nDein Hackaton-Team";

        sendEmail(toEmail, "Einladung zur Gruppe: " + groupName, message);
    }
}
