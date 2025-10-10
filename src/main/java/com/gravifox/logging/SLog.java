package com.gravifox.logging;

import com.slack.api.Slack;
import com.slack.api.webhook.WebhookResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import static com.slack.api.webhook.WebhookPayloads.payload;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.slack.api.model.Attachment;

@Slf4j
@Service
public class SLog {
    @Value("${logging.slack.webhook-uri:}")
    private String u;
    private final Slack slack = Slack.getInstance();

    public void info(String m) {
        log.info("staart log m {}",m);
        try{
            HttpServletRequest request = LoggingFilter.getRequest();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            String sb = ":ballot_box_with_check: Log Message" + "\n" +
                    "```" + m + "```" +
                    ":white_check_mark: Request URI" + "\n" +
                    "```" + request.getMethod() + " - " + request.getRequestURI() + "```" +
                    ":white_check_mark: Request ID" + "\n" +
                    "```" + request.getHeader("X-RequestID") + "```" +
                    ":white_check_mark: Request Address" + "\n" +
                    "```" + request.getHeader("X-Forwarded-For") + "```";

            WebhookResponse r = slack.send(u, payload(p -> p
                    .attachments(List.of(
                            Attachment.builder()
                                    .fallback("Slack Error Message")
                                    .pretext("*INFO * - " + LocalDateTime.now().format(formatter))
                                    .text(sb)
                                    .color("#36a64f")
                                    .build()
                    ))

            ));
            log.info(r.getBody());
            log.info(r.getMessage());
        } catch (IOException error) {
            log.error(error.getMessage());
            log.info("failed to send log message");
    }
    log.info("successfully sent log message");
//    public void send(Exception e) {
//        try {
//            Payload p = Payload.builder().
//                    text(e.getMessage()).
//                    build();
//            slack.send(u, p);
//            log.info("SUCCESS!!");
//        } catch (IOException error) {
//            log.error("slack 메시지 발송 중 문제가 발생.");
//        }
    }

}
