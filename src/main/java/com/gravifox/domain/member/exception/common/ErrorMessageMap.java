package com.gravifox.domain.member.exception.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "API 에러 응답 형식")
@Getter
@AllArgsConstructor
public class ErrorMessageMap {

    @Schema(description = "에러 코드", example = "INVALID_CREDENTIALS")
    private String code;

    @Schema(description = "에러 메세지", example = "Oops! Something doesn’t match.")
    private String message;
}
