package com.gravifox.domain.member.service.impl;

import com.gravifox.annotation.LogContext;
import com.gravifox.domain.admin.domain.Term;
import com.gravifox.domain.admin.repository.TermRepository;
import com.gravifox.domain.dashboard.domain.Dashboard;
import com.gravifox.domain.dashboard.repository.DashboardRepository;
import com.gravifox.domain.member.domain.UserTerm;
import com.gravifox.domain.member.domain.user.LoginType;
import com.gravifox.domain.member.dto.register.RegisterRequest;
import com.gravifox.domain.member.dto.register.RegisterResponse;
import com.gravifox.domain.member.domain.Password;
import com.gravifox.domain.member.domain.Profile;
import com.gravifox.domain.member.domain.user.User;
import com.gravifox.domain.member.exception.register.DataIntegrityViolationException;
import com.gravifox.domain.member.repository.ProfileRepository;
import com.gravifox.domain.member.repository.UserRepository;
import com.gravifox.domain.member.repository.PasswordRepository;
import com.gravifox.domain.member.repository.UserTermRepository;
import com.gravifox.domain.member.service.RegisterService;
import com.gravifox.domain.member.service.EmailVerificationService;
import com.gravifox.domain.member.domain.verification.VerificationPurpose;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class RegisterServiceImpl implements RegisterService {
    @Autowired private UserRepository userRepository;
    @Autowired private ProfileRepository profileRepository;
    @Autowired private PasswordRepository passwordRepository;
    @Autowired private UserTermRepository userTermRepository;
    @Autowired private TermRepository termRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private DashboardRepository dashboardRepository;

    @Autowired
    private EmailVerificationService emailVerificationService;

    @Override
    @Transactional
    @LogContext(action = "UserRegistration", detail="UserId")
    public RegisterResponse toRegisterUser(RegisterRequest registerRequestData) {
        RegisterRequest.UserRequestData userD_ = registerRequestData.getUser();
        RegisterRequest.ProfileRequestData profileD_ = registerRequestData.getProfile();
        RegisterRequest.PasswordRequestData passwordD_ = registerRequestData.getPassword();
        RegisterRequest.TermsRequestData termsD_ = registerRequestData.getTerms();

        userRepository.findByUserId(userD_.getUserId()).ifPresent(user -> {
            throw DataIntegrityViolationException.forDuplicateUserId(userD_.getUserId());
        });
        profileRepository.findByNickname(profileD_.getNickname()).ifPresent(profile -> {
            //todo 지금은 프론트엔드에서 닉네임 적는 곳이 없어 랜덤 값이 들어가지만 차후 닉네임이 설정되면 예외처리 개선 필요
            throw new DataIntegrityViolationException();
        });

        User user = User.builder()
                .userId(userD_.getUserId())
                .loginType(LoginType.fromString(userD_.getLoginType()))
                .build();

        userRepository.save(user);

        Profile profile = Profile.builder()
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .nickname(profileD_.getNickname())
                .user(user)
                .build();
       profileRepository.save(profile);
       // 양방향 관계 동기화로 조회 시 user.getProfile() null 문제 방지
       user.setProfile(profile);

        Password password = Password.builder()
                .password(passwordEncoder.encode(passwordD_.getPassword()))
                .updatedAt(LocalDateTime.now())
                .user(user)
                .build();
        passwordRepository.save(password);

        Dashboard dashboard = Dashboard.builder()
                .user(user)
                .apiKey("")
                .build();
        dashboardRepository.save(dashboard);
        List<Term> terms = termRepository.findAll();
        List<UserTerm> userTerms = terms.stream()
                        .map(term -> UserTerm.builder()
                                .userNo(user.getUserNo())
                                .termId(term.getTermId())
                                .agreedVersion(term.getTermVersion())
                                .build())
                .toList();

        userTermRepository.saveAll(userTerms);

        // 이메일 인증 요청 자동 발송 (회원가입 완료 직후)
        try {
            emailVerificationService.requestVerification(user.getUserId(), VerificationPurpose.SIGNUP);
        } catch (Exception e) {
            // 발송 실패가 회원가입 트랜잭션을 막지 않도록 예외는 삼킵니다. (로그는 AOP/전역 로거에서 처리)
        }

        return toRegisterResponse(user);
    }
    private RegisterResponse toRegisterResponse(User user) {
        return new RegisterResponse(user.getUserId());
    }
}
