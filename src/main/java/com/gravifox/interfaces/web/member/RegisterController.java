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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Tag(name = "사용자 계정 생성", description = "사용자 회원가입 API")
@RestController
@RequestMapping("api/v1/register")
@RequiredArgsConstructor
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
            pwd.getPassword().matches("^(?=.*[A-Za-z])(?=.*[A-Z])(?=.*\\\\d)(?=.*[!@#$%^&*()_+\\\\-={}\\\\[\\\\]:\\\";'<>?,./]).{8,20}$");
    Predicate<RegisterRequest.UserRequestData> userIdValidator = user ->
            !user.getUserId().isBlank() &&
                    user.getUserId().matches("^[a-zA-Z0-9_+&*-]+(?:\\\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\\\.)+[a-zA-Z]{2,7}$");
    Predicate<RegisterRequest.ProfileRequestData> profileValidator = profile ->
            !profile.getNickname().isBlank();
    Predicate<RegisterRequest.UserRequestData> loginTypeValidator = user ->
            !user.getLoginType().isBlank();

    Consumer<RegisterRequest> validateRequestData = r -> {
        if (!userIdValidator.test(r.getUser())) {
            throw InvalidFormatException.forInvalidUserId(r.getUser().getUserId());
        }
        if (!passswordValidator.test(r.getPassword())) {
            throw InvalidFormatException.forInvalidPassword(r.getPassword().getPassword());
        }
        if (!profileValidator.test(r.getProfile())) {
            throw InvalidFormatException.forInvalidNickName(r.getProfile().getNickname());
        }
        if (!loginTypeValidator.test(r.getUser())) {
            throw InvalidFormatException.forInvalidLoginType(r.getUser().getLoginType());
        }
    };
}

