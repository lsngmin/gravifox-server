package com.gravifox.tvb.domain.notification;

public interface MailSender {
    void send(String to, String subject, String htmlBody);
}

