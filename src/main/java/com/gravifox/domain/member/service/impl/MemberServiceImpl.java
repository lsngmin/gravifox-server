package com.gravifox.domain.member.service.impl;

import com.gravifox.domain.member.domain.Password;
import com.gravifox.domain.member.domain.Profile;
import com.gravifox.domain.member.domain.user.LoginType;
import com.gravifox.domain.member.domain.user.User;
import com.gravifox.domain.member.dto.mypage.MyInfoResponse;
import com.gravifox.domain.member.dto.mypage.PasswordChangeRequest;
import com.gravifox.domain.member.dto.mypage.ProfileUpdateRequest;
import com.gravifox.domain.member.exception.InvalidCredentialsException;
import com.gravifox.domain.member.exception.common.ErrorCode;
import com.gravifox.domain.member.exception.register.DataIntegrityViolationException;
import com.gravifox.domain.member.exception.register.InvalidFormatException;
import com.gravifox.domain.member.exception.user.UserNotFoundException;
import com.gravifox.domain.member.repository.PasswordRepository;
import com.gravifox.domain.member.repository.ProfileRepository;
import com.gravifox.domain.member.repository.SocialLoginRepository;
import com.gravifox.domain.member.repository.UserRepository;
import com.gravifox.domain.member.service.MemberService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final UserRepository userRepository;
    private final PasswordRepository passwordRepository;
    private final ProfileRepository profileRepository;
    private final SocialLoginRepository socialLoginRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo(Long userNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new UserNotFoundException(userNo));

        Profile profile = resolveProfile(user);

        String uid = user.getUserId();
        if (user.getLoginType() == LoginType.GOOGLE && user.getSocialLogin() != null && user.getSocialLogin().getSocialId() != null) {
            uid = user.getSocialLogin().getSocialId();
        }

        return MyInfoResponse.builder()
                .userId(uid)
                .loginType(user.getLoginType().name())
                .nickname(profile.getNickname())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteAccount(Long userNo) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new UserNotFoundException(userNo));

        passwordRepository.deleteByUser(user);
        socialLoginRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userNo, PasswordChangeRequest request) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new UserNotFoundException(userNo));

        // 소셜 로그인 사용자는 비밀번호 변경 불가
        if (user.getLoginType() != LoginType.EMAIL) {
            throw new InvalidCredentialsException();
        }

        Password passwordEntity = passwordRepository.findByUser(user)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.currentPassword(), passwordEntity.getPassword())) {
            throw new InvalidCredentialsException();
        }

        passwordEntity.updatePassword(passwordEncoder.encode(request.newPassword()));
    }

    @Override
    @Transactional
    public MyInfoResponse updateProfile(Long userNo, ProfileUpdateRequest request) {
        User user = userRepository.findById(userNo)
                .orElseThrow(() -> new UserNotFoundException(userNo));

        String nickname = request.nickname() == null ? null : request.nickname().trim();
        if (nickname == null || nickname.isBlank()) {
            throw new InvalidFormatException(
                    ErrorCode.INVALID_NICKNAME_ERROR,
                    "Nickname cannot be blank");
        }

        // 중복 닉네임 체크(본인 제외)
        profileRepository.findByNickname(nickname).ifPresent(existing -> {
            if (!existing.getUser().getUserNo().equals(userNo)) {
                throw new DataIntegrityViolationException();
            }
        });

        Profile profile = resolveProfile(user);
        profile.updateNickname(nickname);
        profileRepository.save(profile);

        return MyInfoResponse.builder()
                .userId(user.getUserId())
                .loginType(user.getLoginType().name())
                .nickname(profile.getNickname())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }

    private Profile resolveProfile(User user) {
        Profile profile = user.getProfile();
        if (profile != null) {
            return profile;
        }

        return profileRepository.findByUser(user)
                .orElseGet(() -> {
                    Profile created = Profile.builder()
                            .nickname(generateFallbackNickname(user))
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .user(user)
                            .build();
                    Profile saved = profileRepository.save(created);
                    user.setProfile(saved);
                    return saved;
                });
    }

    private String generateFallbackNickname(User user) {
        String base = user.getUserId();
        if (base == null || base.isBlank()) {
            base = "user";
        }
        base = base.replaceAll("[^a-zA-Z0-9]", "");
        if (base.isBlank()) {
            base = "user";
        }

        String candidate = base;
        int suffix = 1;
        while (profileRepository.findByNickname(candidate).isPresent()) {
            candidate = base + suffix;
            suffix++;
            if (suffix > 1000) {
                candidate = base + Long.toHexString(System.currentTimeMillis());
                break;
            }
        }
        return candidate;
    }
}
