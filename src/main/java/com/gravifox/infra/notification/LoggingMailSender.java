package com.gravifox.infra.notification;

import com.gravifox.domain.notification.MailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@ConditionalOnProperty(name = "mail.sender", havingValue = "log", matchIfMissing = true)
public class LoggingMailSender implements MailSender {
    @Override
    public void send(String to, String subject, String htmlBody) {
        int length = htmlBody == null ? 0 : htmlBody.length();
        String preview = htmlBody == null ? "" : htmlBody.replaceAll("\n", " ");
        if (preview.length() > 300) {
            preview = preview.substring(0, 300) + "...";
        }
        // Try to extract first link (href)
        String link = null;
        if (htmlBody != null) {
            Pattern p = Pattern.compile("href=['\"]([^'\"]+)['\"]");
            Matcher m = p.matcher(htmlBody);
            if (m.find()) {
                link = m.group(1);
            }
        }
        log.info("[MAIL][DEV] to={}, subject={}, length={} chars", to, subject, length);
        if (link != null) {
            log.info("[MAIL][DEV] verificationLink={}", link);
        }
        log.debug("[MAIL][DEV] bodyPreview={}", preview);
    }
}
