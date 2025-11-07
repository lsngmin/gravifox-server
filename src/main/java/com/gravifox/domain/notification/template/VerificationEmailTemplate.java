package com.gravifox.domain.notification.template;

public final class VerificationEmailTemplate {
    private VerificationEmailTemplate() {}

    public static String render(String serviceName, String verifyLink, String lang) {
        String safeService = serviceName == null ? "서비스" : serviceName;
        String language = normalizeLanguage(lang);
        LangCopy copy = LangCopy.forLanguage(language);
        String ctaLink = appendLangParam(verifyLink, language);
        String template = """
            <div style='margin:0;padding:32px 0;background:#f5f7fb;font-family:-apple-system,BlinkMacSystemFont,"Segoe UI",Roboto,Arial,sans-serif;'>
              <table role='presentation' cellpadding='0' cellspacing='0' style='width:100%%;max-width:640px;margin:0 auto;background:#ffffff;border-radius:28px;padding:40px 32px;box-shadow:0 25px 60px -30px rgba(15,23,42,0.45);'>
                <tr>
                  <td style='text-align:center;padding-bottom:24px;'>
                    <p style='margin:0 0 12px;font-size:12px;letter-spacing:0.4em;text-transform:uppercase;color:#7c8db5;'>%s</p>
                    <h1 style='margin:0;font-size:24px;line-height:1.4;color:#0f172a;'>%s</h1>
                  </td>
                </tr>
                <tr>
                  <td style='font-size:15px;line-height:1.7;color:#475467;padding-bottom:28px;text-align:center;'>
                    %s
                  </td>
                </tr>
                <tr>
                  <td style='text-align:center;padding-bottom:32px;'>
                    <a href='%s' style='display:inline-block;padding:14px 28px;border-radius:999px;background-image:linear-gradient(135deg,#4338ca,#4f46e5,#0ea5e9);color:#ffffff;font-size:15px;font-weight:600;text-decoration:none;'>%s</a>
                  </td>
                </tr>
                <tr>
                  <td style='font-size:13px;line-height:1.7;color:#667085;padding-bottom:20px;'>
                    %s
                  </td>
                </tr>
                <tr>
                  <td style='font-size:12px;line-height:1.6;color:#475467;background:#f4f6fb;border-radius:16px;padding:16px 18px;word-break:break-all;'>
                    <a href='%s' style='color:#4338ca;text-decoration:none;'>%s</a>
                  </td>
                </tr>
                <tr>
                  <td style='padding-top:40px;font-size:13px;line-height:1.6;color:#94a3b8;text-align:center;'>
                    %s
                  </td>
                </tr>
              </table>
              <p style='margin:16px auto 0;text-align:center;font-size:11px;color:#94a3b8;max-width:480px;line-height:1.6;'>
                %s
              </p>
            </div>
            """;
        return String.format(
                template,
                copy.eyebrow(),
                copy.heroTitle(safeService),
                copy.bodyText(),
                ctaLink, copy.buttonText(),
                copy.fallbackIntro(),
                ctaLink, ctaLink,
                copy.closing(safeService),
                copy.footnote()
        );
    }

    private static String appendLangParam(String base, String langCode) {
        if (langCode == null || langCode.isBlank() || base == null || base.isBlank()) {
            return base;
        }
        String delimiter = base.contains("?") ? "&" : "?";
        return base + delimiter + "lang=" + langCode;
    }
    private static String normalizeLanguage(String lang) {
        if (lang == null) return "ko";
        String normalized = lang.trim().toLowerCase();
        return switch (normalized) {
            case "en" -> "en";
            default -> "ko";
        };
    }

    private record LangCopy(
            String languageLabel,
            String koLabel,
            String enLabel,
            String eyebrow,
            String heroTitleFormat,
            String bodyText,
            String buttonText,
            String fallbackIntro,
            String closingFormat,
            String footnote
    ) {
        static LangCopy forLanguage(String language) {
            if ("en".equals(language)) {
                return new LangCopy(
                        "Language",
                        "한국어",
                        "English",
                        "EMAIL VERIFICATION",
                        "Activate your %s account",
                        "To finish signing up, tap the button below to verify your email.<br />This link expires after a short period.",
                        "Verify Email",
                        "If the button doesn't work, copy and paste the link below into your browser.",
                        "Thank you,<br />The %s Team",
                        "This is an automated message. If you didn't request access, please ignore it."
                );
            }
            return new LangCopy(
                    "언어",
                    "한국어",
                    "English",
                    "EMAIL VERIFICATION",
                    "%s 계정을 활성화해 주세요",
                    "가입을 계속 진행하려면 아래 버튼을 눌러 이메일을 인증해 주세요.<br />이 링크는 일정 시간 후 만료됩니다.",
                    "이메일 인증하기",
                    "버튼이 동작하지 않는다면 아래 링크를 복사해 브라우저 주소창에 붙여 넣어 주세요.",
                    "감사합니다.<br />%s 팀 드림",
                    "본 메일은 발신 전용입니다. 가입을 요청하지 않은 경우 이 메일을 무시하셔도 됩니다."
            );
        }

        String heroTitle(String serviceName) {
            return String.format(heroTitleFormat, serviceName);
        }

        String closing(String serviceName) {
            return String.format(closingFormat, serviceName);
        }
    }
}
