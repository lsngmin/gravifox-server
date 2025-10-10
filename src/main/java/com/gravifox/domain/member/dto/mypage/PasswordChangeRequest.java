package com.gravifox.domain.member.dto.mypage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청 DTO")
public record PasswordChangeRequest(
        @NotBlank(message = "currentPassword must not be blank")
        @Schema(description = "현재 비밀번호", example = "OldPassword123!") String currentPassword,

        @NotBlank(message = "newPassword must not be blank")
        @Size(min = 8, max = 20, message = "newPassword length must be 8-20")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).+$",
                message = "newPassword must include letters, numbers, and special characters"
        )
        @Schema(description = "새 비밀번호", example = "NewPassword123!") String newPassword
) {}
