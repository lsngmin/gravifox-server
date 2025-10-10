package com.gravifox.domain.member.controller;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Tag(
        name="사용자 계정 생성",
        description = "```사용자가 계정을 생성하는 데 필요한 API```  <i>이메일</i>, <i>비밀번호</i> 등 필수 정보를 수집하여 신규 사용자를 등록합니다."
)
@RestController
@RequestMapping("api/v1/register")
@RequiredArgsConstructor
public class RegisterController {
    private final RegisterService registerService;
    private final UserTermService userTermService;

    @Operation(
            summary = "사용자 등록",
            description =  " 이 API는 사용자로부터 받은 요청 데이터를 검증한 후, 유효한 경우 새 사용자 등록을 처리합니다.<br/>"+
                    "시스템은 제공된 이메일이 이미 등록된 이메일이 아닌지 확인하고, 필요한 데이터가 유효한지 검증합니다. 유효하지 않은 데이터나 중복된 이메일이 있을 경우 적절한 오류 응답이 반환됩니다.<br/> " +
                    "등록이 성공하면 새로 생성된 사용자 정보가 포함된 응답을 반환합니다. 이 엔드포인트는 사전 로그인이나 인증이 필요하지 않지만, 이메일 인증은 향후 구현될 수 있습니다.<br/> " +
                    "또한 입력 필드에 대한 기본 유효성 검사를 수행하여 데이터 무결성을 보장합니다.\n",
            responses = {
                    @ApiResponse(responseCode = "200", description = "사용자 등록 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = RegisterResponse.class))),
                    @ApiResponse(responseCode = "422", description = "잘못된 요청 - 요청 데이터의 유효성 검사 실패", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessageMap.class)))
            }
    )
    @PostMapping
    @LogContext(action = "UserRegistration", detail="UserId")
    public ResponseEntity<RegisterResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequestData) {
        //RequestData의 유효성 검증 로직입니다.
        validateRequestData.accept(registerRequestData);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerService.toRegisterUser(registerRequestData));
    }

    Predicate<RegisterRequest.PasswordRequestData> passswordValidator = pwd ->
            pwd.getPassword().matches("^(?=.*[A-Za-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:\";'<>?,./]).{8,20}$");
    Predicate<RegisterRequest.UserRequestData> userIdValidator = user ->
        !user.getUserId().isBlank() &&
                user.getUserId().matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    Predicate<RegisterRequest.ProfileRequestData> profileValidator = profile ->
            !profile.getNickname().isBlank();
    Predicate<RegisterRequest.UserRequestData> loginTypeValidator = user ->
            !user.getLoginType().isBlank();


    Consumer<RegisterRequest> validateRequestData = r -> {
        if(!userIdValidator.test(r.getUser())) {
            throw InvalidFormatException.forInvalidUserId(r.getUser().getUserId());
        }
        if(!passswordValidator.test(r.getPassword())) {
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
