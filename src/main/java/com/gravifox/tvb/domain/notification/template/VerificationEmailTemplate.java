package com.gravifox.tvb.domain.notification.template;

public final class VerificationEmailTemplate {
    private VerificationEmailTemplate() {}

    public static String render(String serviceName, String verifyLink) {
        String safeService = serviceName == null ? "서비스" : serviceName;
        String template = """
            <div style='font-family:Arial,Helvetica,sans-serif;max-width:560px;margin:0 auto;padding:24px;color:#111;'>
              <h1 style='font-size:20px;margin:0 0 16px;'>%s 이메일 인증</h1>
              <p style='line-height:1.6;margin:0 0 16px;'>아래 버튼을 클릭하여 이메일 인증을 완료해 주세요.</p>
              <p style='margin:24px 0;'>
                <a href='%s' style='display:inline-block;background:#111;color:#fff;text-decoration:none;padding:12px 16px;border-radius:6px;'>인증하기</a>
              </p>
              <p style='font-size:12px;color:#666;line-height:1.6;'>버튼이 동작하지 않으면 아래 링크를 복사해 브라우저 주소창에 붙여넣기 해주세요.</p>
              <p style='font-size:12px;color:#666;word-break:break-all;'>%s</p>
            </div>
            """;
        return String.format(template, safeService, verifyLink, verifyLink);
    }
}
