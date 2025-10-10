package com.gravifox.domain.member.dto.login;

import com.gravifox.domain.member.dto.AuthDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

//
@Schema(
        description = "사용자 로그인 응답 DTO",
        example = """
        {
        "userId": "user@example.com",
        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        }
        """
)
@Data
public class LoginResponse implements AuthDTO {

    @Schema(
            description = "사용자의 이메일",
            example = "user@example.com",
            required = true
    )
    private String userId;

    @Schema(
            description = "로그인 성공 시 발급되는 JWT 액세스 토큰",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
            required = true
    )
    private String accessToken;

    @Override
    public String extractUserID() {
        return this.userId;
    }
}
