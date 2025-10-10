package com.gravifox.domain.member.dto.register;

import com.gravifox.domain.member.domain.user.User;
import com.gravifox.domain.member.dto.AuthDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//
@Schema(
        description = "사용자 회원가입 정보 DTO",
        example = """
    {
      "user": {
        "userId": "user@example.com",
        "loginType": "EMAIL"
      },
      "profile": {
        "nickname": "hellojungjae",
        "createdAt": "2025-04-28T12:34:56",
        "updatedAt": "2025-04-29T11:03:54"
      },
      "password": {
        "password": "StrongP@ssword",
        "updatedAt": "2025-04-29T14:05:32"
      }
      "terms": {
        "termsCookie":"True",
        "termsMarketing":"True"
    }
    """
)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest implements AuthDTO {

    @Schema(description = "사용자 기본 정보" ,required = true)
    @Valid @NotNull private UserRequestData user;

    @Schema(description = "사용자 프로필", required = true)
    @Valid @NotNull private ProfileRequestData profile;

    @Schema(description = "사용자 비밀번호", required = true)
    @Valid @NotNull private PasswordRequestData password;

    @Schema(description = "사용자 약관동의 정보", required = true)
    @Valid @NotNull private TermsRequestData terms;

    @Override
    public String extractUserID() {
        return this.user.getUserId();
    }

    @Schema(
            description = "회원가입 시 사용자 계정 정보",
            example = """
    {
      "userId": "user@example.com",
      "loginType": "EMAIL"
    }
    """
    )
    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserRequestData {

        @Schema(description = "사용자 이메일(ID)", example = "user@example.com", required = true)
        @NotNull private String userId;

        @Schema(description = "로그인 방식", example = "EMAIL", required = true)
        @NotNull String loginType;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(
            description = "회원가입 시 사용자 기본 정보",
            example = """
    {
      "nickname": "hellojungjae",
      "createdAt": "2025-04-28T12:34:56",
      "updatedAt": "2025-04-29T11:03:54"
    }
    """
    )
        public static class ProfileRequestData {
        @Schema(description = "사용자 닉네임" , example = "홍길동", required = true)
        @NotNull private String nickname;

        @Schema(description = "프로필 생성일시", example = "2025-04-28T12:34:56")
        private LocalDateTime createdAt;

        @Schema(description = "프로필 수정일시", example = "2025-04-29T11:03:54")
        private LocalDateTime updatedAt;

        @Schema(hidden = true)
        private User user;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(
            description = "회원가입 시 사용자 비밀번호 정보",
            example = """
            {
                "password": "StrongP@ssword",
                "updatedAt": "2025-04-29T14:05:32"
            }
            """
        )
    public static class PasswordRequestData {
        @Schema(description = "비밀번호", example = "StrongP@ssword",required = true)
        @NotNull private String password;

        @Schema(description = "비밀번호 마지막 수정일시" , example = "2025-04-29T14:05:32")
        private LocalDateTime updatedAt;

        @Schema(hidden = true)
        private User user;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Schema(
            description = "회원가입 시 사용자 이용약관 동의 정보",
            example = """
            {
                "termsCookie": "true",
                "termsMarketing": "false"
            }
            """
    )
    public static class TermsRequestData {
        @Schema(description = "쿠키 이용약관", example = "true", required = true)
        @NotNull private String termsCookie;

        @Schema(description = "마케팅 이용약관", example = "true", required = true)
        @NotNull private String termsMarketing;


    }
}
