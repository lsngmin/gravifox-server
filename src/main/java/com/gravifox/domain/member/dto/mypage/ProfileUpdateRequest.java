package com.gravifox.domain.member.dto.mypage;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 업데이트 요청 DTO")
public record ProfileUpdateRequest(
        @Schema(description = "변경할 닉네임", example = "new_nickname") String nickname
) {}

