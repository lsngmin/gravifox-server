package com.gravifox.tvb.infra.notification;

import com.gravifox.tvb.domain.notification.MailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.Body;
import software.amazon.awssdk.services.sesv2.model.Content;
import software.amazon.awssdk.services.sesv2.model.Destination;
import software.amazon.awssdk.services.sesv2.model.EmailContent;
import software.amazon.awssdk.services.sesv2.model.Message;
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest;

@Slf4j
@Service
@ConditionalOnProperty(name = "mail.sender", havingValue = "ses")
@RequiredArgsConstructor
public class SesMailSender implements MailSender {

    private final SesV2Client sesV2Client;

    @Value("${ses.from-email:${SES_FROM_EMAIL:}}")
    private String fromEmail;

    @Override
    public void send(String to, String subject, String htmlBody) {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.error("SES fromEmail not configured");
            return;
        }
        try {
            int length = htmlBody == null ? 0 : htmlBody.length();
            log.info("[MAIL][SES] to={}, subject={}, length={} chars", to, subject, length);
            Destination destination = Destination.builder().toAddresses(to).build();

            Content sub = Content.builder().data(subject).charset("UTF-8").build();
            Content html = Content.builder().data(htmlBody).charset("UTF-8").build();
            Message msg = Message.builder()
                    .subject(sub)
                    .body(Body.builder().html(html).build())
                    .build();

            EmailContent emailContent = EmailContent.builder().simple(msg).build();

            SendEmailRequest req = SendEmailRequest.builder()
                    .fromEmailAddress(fromEmail)
                    .destination(destination)
                    .content(emailContent)
                    .build();

            var resp = sesV2Client.sendEmail(req);
            if (resp != null && resp.messageId() != null) {
                log.info("[MAIL][SES] sent messageId={}", resp.messageId());
            }
        } catch (Exception e) {
            log.error("SES send failed: {}", e.getMessage());
        }
    }
}
