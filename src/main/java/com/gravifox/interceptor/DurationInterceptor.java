package com.gravifox.interceptor;

import com.gravifox.logging.util.LogFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class DurationInterceptor implements HandlerInterceptor {
    private final LogFactory logFactory;
    private static final String START_TIME = "startTime";

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String className = handlerMethod.getBeanType().getSimpleName();
        Long startTime = (Long) request.getAttribute(START_TIME);
        long endTime = System.currentTimeMillis();
        String duration = (endTime - startTime) + "ms";
//fixme duration을 기존 로그 출력문에 이어붙히는 리팩토링 필요
//        log.info(logFactory.of(request, response, className, duration)
//                .action("RequestDuration")
//                .status(LogStatus.OK)
//                .detail("Duration")
//                .build());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME, startTime);
        return true;
    }
}
