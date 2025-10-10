package com.gravifox.logging.util;

import com.gravifox.annotation.LogContext;
import com.gravifox.domain.member.exception.common.ErrorCode;
import com.gravifox.exception.GlobalException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class LogFactory {
    private final LogUtil logUtil;

    @Autowired
    public LogFactory(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    public LogBuilder of(HttpServletRequest request, LogContext logContext, String className) {
        return new LogBuilder(request, (ResponseEntity<?>) null, logContext, className, logUtil);
    }
    public LogBuilder of(HttpServletRequest request, ResponseEntity<?> response, LogContext logContext, String className) {
        return new LogBuilder(request, response, logContext, className, logUtil);
    }
    public LogBuilder of(HttpServletRequest request, GlobalException ex, LogContext logContext, String className) {
        return new LogBuilder(request, ex, logContext, className, logUtil);
    }
    public LogBuilder of(HttpServletRequest request, HttpServletResponse response, String className, String duration) {
        return new LogBuilder(request, response, className, duration, logUtil);
    }

    public static class LogBuilder {
        private final LogMessage.LogMessageBuilder builder;
        private final LogUtil logUtil;

        public LogBuilder(HttpServletRequest request, HttpServletResponse response, String className, String duration, LogUtil logUtil) {
            String statusCode = response != null ? Integer.toString(response.getStatus()) : "";

            this.builder = LogMessage.builder()
                    .method(request.getMethod())
                    .uri(request.getRequestURI())
                    .code(statusCode)
                    .className(className)
                    .value(duration);

            this.logUtil = logUtil;
        }

        public LogBuilder(HttpServletRequest request,
                          GlobalException e,
                          LogContext logContext,
                          String className,
                          LogUtil logUtil) {

            this.builder = LogMessage.builder()
                    .method(request.getMethod())
                    .uri(request.getRequestURI())
                    .code(Integer.toString(e.getErrorCode().getHttpStatus().value()))
                    .action(logContext.action())
                    .status(logContext.status())
                    .detail(e.getMessage())
                    .className(className);

            this.logUtil = logUtil;

        }

        public LogBuilder(HttpServletRequest request,
                          ResponseEntity<?> response,
                          LogContext logContext,
                          String className,
                          LogUtil logUtil) {
            String statusCode = response != null ? Integer.toString(response.getStatusCode().value()) : "";

            this.builder = LogMessage.builder()
                    .method(request.getMethod())
                    .uri(request.getRequestURI())
                    .code(statusCode)
                    .action(logContext.action())
                    .status(logContext.status())
                    .detail(logContext.detail())
                    .className(className);
            this.logUtil = logUtil;
        }

        public LogBuilder className(String c) {builder.className(c); return this;}
        public LogBuilder method(String m) { builder.method(m); return this; }
        public LogBuilder uri(String u) { builder.uri(u); return this; }
        public LogBuilder status(LogStatus s) { builder.status(s.getStatus()); return this; }
        public LogBuilder status(ErrorCode e) { builder.status(e.getCode()); return this; }

        public LogBuilder detail(String d) { builder.detail(d); return this; }
        public LogBuilder value(String v) { builder.value(mask(v)); return this; }
        public LogBuilder action(String a) { builder.action(a); return this; }
        public LogBuilder code(String c) { builder.action(c); return this; }

        public String build() {
            return builder.build().formatMessage();
        }

        private String mask(String v) {
            return logUtil.maskValue(v);
        }
    }
}
