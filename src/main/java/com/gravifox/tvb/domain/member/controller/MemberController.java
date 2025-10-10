package com.gravifox.tvb.domain.member.controller;

import com.gravifox.tvb.domain.member.domain.user.User;
import com.gravifox.tvb.domain.member.dto.mypage.MyInfoResponse;
import com.gravifox.tvb.domain.member.dto.mypage.PasswordChangeRequest;
import com.gravifox.tvb.domain.member.dto.mypage.ProfileUpdateRequest;
import com.gravifox.tvb.domain.member.exception.user.UserNotFoundException;
import com.gravifox.tvb.domain.member.repository.UserRepository;
import com.gravifox.tvb.domain.member.service.MemberService;
import com.gravifox.tvb.security.jwt.principal.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.ZoneOffset;

//
@Tag(
        name = "내 사용자 정보 조회",
        description = """
        로그인한 사용자가 자신의 정보를 확인할 수 있는 API입니다.
        클라이언트는 JWT Access Token을 Authorization 헤더에 담아 요청해야 하며,
        서버는 토큰을 통해 인증된 사용자 정보를 반환합니다.
        이 API는 마이페이지 조회 또는 프로필 편집 화면 등에 활용됩니다.
    """
)
@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final UserRepository userRepository;
    @Operation(
            summary = "내 사용자 정보 조회",
            description = """
                로그인한 사용자가 본인의 정보를 조회하는 API입니다.
                클라이언트는 Authorization 헤더에 Access Token을 포함해야 하며,
                서버는 토큰을 검증하여 인증된 사용자의 정보를 반환합니다.
    
                반환되는 정보에는 사용자 ID, 이메일, 이름, 가입일 등이 포함됩니다.
            """,
            responses = {
                        @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MyInfoResponse.class))),
                        @ApiResponse(responseCode = "422", description = "토큰은 존재하나 유효하지 않거나 사용자 정보가 존재하지 않음", content = @Content(mediaType = "application/json"))
            }
    )

    @GetMapping("/")
    public ResponseEntity<MyInfoResponse> getMyInfo(
            Authentication authentication,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch
    ) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Long userNo = Long.parseLong(userPrincipal.getName());

        // Build weak ETag based on userNo and profile.updatedAt
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
                                               @RequestBody PasswordChangeRequest request) {
        Long userNo = Long.parseLong(principal.getName()); // getName() = userNo (String)
        memberService.changePassword(userNo, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/")
    public ResponseEntity<MyInfoResponse> updateProfile(Authentication authentication,
                                                        @RequestBody ProfileUpdateRequest request) {
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
