package com.gravifox.domain.member.dto.mypage;

import com.gravifox.domain.member.dto.AuthDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(
        description = "내 정보 조회 응답 DTO",
        example = """
    {
      "userId": "user@example.com",
      "loginType": "EMAIL",
      "nickname": "hellojungjae",
      "createdAt": "2024-05-01T15:20:00"
    }
    """
)
@Getter
@Builder
public class MyInfoResponse implements AuthDTO {

    @Schema(description = "사용자 고유 ID 또는 이메일", example = "user@example.com", required = true)
    private final String userId;

    @Schema(description = "로그인 유형", example = "EMAIL / GOOGLE")
    private final String loginType;

    @Schema(description = "사용자 닉네임", example = "hellojungjae")
    private final String nickname;

    @Schema(description = "회원 가입일", example = "2024-05-01T15:20:00")
    private final LocalDateTime createdAt;

    @Override
    public String extractUserID() {
        return this.userId;
    }
}
