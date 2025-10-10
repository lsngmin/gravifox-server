package com.gravifox.interfaces.web.member;

import com.gravifox.domain.member.dto.mypage.MyInfoResponse;
import com.gravifox.domain.member.dto.mypage.PasswordChangeRequest;
import com.gravifox.domain.member.dto.mypage.ProfileUpdateRequest;
import com.gravifox.domain.member.exception.user.UserNotFoundException;
import com.gravifox.domain.member.repository.UserRepository;
import com.gravifox.domain.member.service.MemberService;
import com.gravifox.security.jwt.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneOffset;

@Tag(
        name = "내 사용자 정보 조회",
        description = "로그인한 사용자가 자신의 정보를 확인하는 API"
)
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final UserRepository userRepository;

    @Operation(
            summary = "내 사용자 정보 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyInfoResponse.class))),
                    @ApiResponse(responseCode = "422", description = "유효하지 않음", content = @Content(mediaType = "application/json"))
            }
    )
    @GetMapping("/")
    public ResponseEntity<MyInfoResponse> getMyInfo(Authentication authentication,
                                                    @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userNo = Long.parseLong(userPrincipal.getName());

        var userOpt = userRepository.findById(userNo);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(userNo);
        }
        var user = userOpt.get();
        var updatedAt = user.getProfile() != null ? user.getProfile().getUpdatedAt() : null;
        long ver = updatedAt != null ? updatedAt.toEpochSecond(ZoneOffset.UTC) : 0L;
        String etag = "W/\"" + userNo + ":" + ver + "\"";

        if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
            return ResponseEntity.status(304).eTag(etag).build();
        }

        MyInfoResponse response = memberService.getMyInfo(userNo);
        return ResponseEntity.ok().eTag(etag).body(response);
    }

    @DeleteMapping("/")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        memberService.deleteAccount(Long.parseLong(userPrincipal.getName()));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                               @Valid @RequestBody PasswordChangeRequest request) {
        Long userNo = Long.parseLong(principal.getName());
        memberService.changePassword(userNo, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/")
    public ResponseEntity<MyInfoResponse> updateProfile(Authentication authentication,
                                                        @Valid @RequestBody ProfileUpdateRequest request) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userNo = Long.parseLong(userPrincipal.getName());

        MyInfoResponse updated = memberService.updateProfile(userNo, request);

        var userOpt = userRepository.findById(userNo);
        var updatedAt = userOpt.map(u -> u.getProfile() != null ? u.getProfile().getUpdatedAt() : null).orElse(null);
        long ver = updatedAt != null ? updatedAt.toEpochSecond(ZoneOffset.UTC) : 0L;
        String etag = "W/\"" + userNo + ":" + ver + "\"";

        return ResponseEntity.ok().eTag(etag).body(updated);
    }
}

