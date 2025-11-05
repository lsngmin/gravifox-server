package com.gravifox.interfaces.web.member;

import com.gravifox.annotation.LogContext;
import com.gravifox.domain.member.dto.register.RegisterRequest;
import com.gravifox.domain.member.dto.register.RegisterResponse;
import com.gravifox.domain.member.exception.common.ErrorMessageMap;
import com.gravifox.domain.member.exception.register.InvalidFormatException;
import com.gravifox.domain.member.service.RegisterService;
import com.gravifox.domain.member.service.UserTermService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Tag(name = "사용자 계정 생성", description = "사용자 회원가입 API")
@RestController
@RequestMapping("api/v1/register")
@RequiredArgsConstructor
@Slf4j
public class RegisterController {
    private final RegisterService registerService;
    private final UserTermService userTermService;

    @Operation(
            summary = "사용자 등록",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용자 등록 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponse.class))),
                    @ApiResponse(responseCode = "422", description = "유효성 검사 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageMap.class)))
            }
    )
    @PostMapping
    @LogContext(action = "UserRegistration", detail = "UserId")
    public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequestData) {
        validateRequestData.accept(registerRequestData);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerService.toRegisterUser(registerRequestData));
    }

    Predicate<RegisterRequest.PasswordRequestData> passswordValidator = pwd ->
            // Hyphen moved to the beginning of the class to avoid range issues
            // Java string escapes: \\\\d -> \d (regex), \\\\[ -> \[, \\\\] -> \]
            pwd.getPassword().matches("^(?=.*[A-Za-z])(?=.*[A-Z])(?=.*\\d)(?=.*[-!@#$%^&*()_+={}\\[\\]:\\\";'<>?,./]).{8,20}$");
    Predicate<RegisterRequest.UserRequestData> userIdValidator = user ->
            !user.getUserId().isBlank() &&
                    // Relax TLD length from 2–7 to 2–63 to allow modern TLDs
                    // Java string needs "\\." to render regex "\."
                    user.getUserId().matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,63}$");
    Predicate<RegisterRequest.ProfileRequestData> profileValidator = profile ->
            !profile.getNickname().isBlank();
    Predicate<RegisterRequest.UserRequestData> loginTypeValidator = user ->
            !user.getLoginType().isBlank();

    Consumer<RegisterRequest> validateRequestData = r -> {
        final String rawUserId = String.valueOf(r.getUser().getUserId());
        final String normalizedUserId = rawUserId == null ? null : rawUserId.trim();

        if (!userIdValidator.test(r.getUser())) {
            log.info("[Register] INVALID_USERID_ERROR raw='{}' normalized='{}'", rawUserId, normalizedUserId);
            throw InvalidFormatException.forInvalidUserId(r.getUser().getUserId());
        }
        if (!passswordValidator.test(r.getPassword())) {
            String pwd = r.getPassword().getPassword();
            int len = pwd == null ? 0 : pwd.length();
            log.info("[Register] INVALID_PASSWORD_ERROR length={} policy='8-20 incl upper/letter/number/special'", len);
            throw InvalidFormatException.forInvalidPassword(r.getPassword().getPassword());
        }
        if (!profileValidator.test(r.getProfile())) {
            String nick = r.getProfile().getNickname();
            int nlen = nick == null ? 0 : nick.trim().length();
            log.info("[Register] INVALID_NICKNAME_ERROR length={} valueBlank={} ", nlen, (nick == null || nick.isBlank()));
            throw InvalidFormatException.forInvalidNickName(r.getProfile().getNickname());
        }
        if (!loginTypeValidator.test(r.getUser())) {
            log.info("[Register] INVALID_LOGINTYPE_ERROR loginType='{}'", r.getUser().getLoginType());
            throw InvalidFormatException.forInvalidLoginType(r.getUser().getLoginType());
        }
    };
}
