package com.gravifox.tvb.domain.member.service;

import com.gravifox.tvb.domain.member.dto.mypage.MyInfoResponse;
import com.gravifox.tvb.domain.member.dto.mypage.PasswordChangeRequest;
import com.gravifox.tvb.domain.member.dto.mypage.ProfileUpdateRequest;

public interface MemberService {
    MyInfoResponse getMyInfo(Long userNo);
    void deleteAccount(Long userNo);
    void changePassword(Long userNo, PasswordChangeRequest request);
    MyInfoResponse updateProfile(Long userNo, ProfileUpdateRequest request);
}
