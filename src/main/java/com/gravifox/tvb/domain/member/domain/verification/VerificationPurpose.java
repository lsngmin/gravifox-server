package com.gravifox.tvb.domain.member.domain.verification;

public enum VerificationPurpose {
    SIGNUP,
    RESET_PASSWORD,
    VERIFY_EMAIL;

    public static VerificationPurpose fromString(String value) {
        if (value == null) return SIGNUP;
        for (VerificationPurpose p : values()) {
            if (p.name().equalsIgnoreCase(value)) {
                return p;
            }
        }
        return SIGNUP;
    }
}
