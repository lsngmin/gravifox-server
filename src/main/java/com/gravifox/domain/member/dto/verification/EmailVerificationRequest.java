package com.gravifox.domain.member.dto.verification;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EmailVerificationRequest {
    private String email;
    private String purpose; // SIGNUP | RESET_PASSWORD | VERIFY_EMAIL
}
