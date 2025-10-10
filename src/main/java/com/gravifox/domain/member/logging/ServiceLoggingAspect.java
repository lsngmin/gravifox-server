package com.gravifox.domain.member.logging;

import com.gravifox.annotation.LogContext;
import com.gravifox.domain.member.exception.auth.AuthException;
import com.gravifox.logging.util.LogFactory;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceLoggingAspect {
    private final LogFactory logFactory;

    @Pointcut("execution(* com.gravifox.tvb.domain.member.service.impl.*.*(..)) && @annotation(logContext)")
    public void myPointcut(LogContext logContext) {}

    @AfterThrowing(pointcut = "myPointcut(logContext)", throwing = "ex")
    public void afterThrowing(JoinPoint joinPoint, AuthException ex, LogContext logContext) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        log.warn(logFactory.of(request, ex, logContext, className).status(ex.getErrorCode()).value(ex.getValue()).build());

//        log.warn(logUtil.formatMessage(
//                "UserRegistration",
//                ex.getClass().getSimpleName(),
//                ex.getMessage(),
//                request.getMethod(),
//                request.getRequestURI(),
//                ex.getErrorCode().getHttpStatus().value(),
//                logUtil.maskValue(registerRequestData.getUser().getUserId())
//        ));
    }
}
