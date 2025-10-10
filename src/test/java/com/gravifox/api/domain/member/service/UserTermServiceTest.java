package com.gravifox.api.domain.member.service;

import com.gravifox.domain.member.repository.UserTermRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
public class UserTermServiceTest {
    @Mock
    private UserTermRepository userTermRepository;

    @Test
    @DisplayName("존재하는 사용자 userNo로 약관에 동의한 내역을 저장할 수 있다.")
    void saveUserTerm_whenUserExists_savesAgreement() {

    }
}
