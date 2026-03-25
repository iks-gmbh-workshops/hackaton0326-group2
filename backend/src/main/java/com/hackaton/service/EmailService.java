package com.hackaton.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendGroupInvitation(String toEmail, String groupName, String inviteToken) {
        String inviteLink = "http://localhost:5173/invite/" + inviteToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Einladung zur Gruppe: " + groupName);
        message.setText(
                "Hallo,\n\n" +
                "du wurdest zur Gruppe \"" + groupName + "\" eingeladen.\n\n" +
                "Klicke auf den folgenden Link, um die Einladung anzunehmen:\n" +
                inviteLink + "\n\n" +
                "Viele Grüße,\nDein Hackaton-Team"
        );

        try {
            mailSender.send(message);
            log.info("Invitation email sent to {} for group '{}'", toEmail, groupName);
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send invitation email", e);
        }
    }
}
