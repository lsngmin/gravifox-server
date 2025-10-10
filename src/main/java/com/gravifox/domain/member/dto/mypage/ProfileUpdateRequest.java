package com.gravifox.domain.member.dto.mypage;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "프로필 업데이트 요청 DTO")
public record ProfileUpdateRequest(
        @NotBlank(message = "nickname must not be blank")
        @Size(min = 1, max = 20, message = "nickname length must be 1-20")
        @Schema(description = "변경할 닉네임", example = "new_nickname") String nickname
) {}
