package com.gravifox.domain.member.dto.register;

import com.gravifox.domain.member.dto.AuthDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(
        description = "회원가입 성공 응답 DTO",
        example = """
    {
      "userId": "user@example.com"
    }
    """
)
@AllArgsConstructor
@Getter
public class RegisterResponse implements AuthDTO {
    @Override
    public String extractUserID() {
        return this.userId;
    }

    @Schema(description = "회원가입된 사용자의 이메일", example = "user@example.com", required = true)
    private String userId;
}
