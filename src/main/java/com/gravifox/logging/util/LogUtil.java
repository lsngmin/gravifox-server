package com.gravifox.logging.util;

import com.gravifox.logging.SLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogUtil {
    private final SLog sLog;

    /**
     * 이 메서드는 더 이상 사용하지 마세요
     * LogMessage와 LogFactory 클래스로 분리되어 사용됩니다.
     * 자세한 내용은 README.md 를 참고하세요
     */
    @Deprecated
    public String formatMessage(String a, String s, String d, Object... args) {
        String f = String.format("\"%-4s %s\" %3s - %s | %-16s | %s(%s)",
                args[0], args[1], args[2], a, s, d, args[3]
        );
        return f;
    }
    public String maskValue(String s) {
        if (s == null) return "";
        char[] c = s.toCharArray();
        for(int i = 3; i < c.length; i++) {
            if(c[i] == '@') break;
            c[i] = '*';
        }
        return new String(c);
    }
}
