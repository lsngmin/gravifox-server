package com.gravifox.api.domain.admin.service;

import com.gravifox.domain.admin.dto.NewTerm;
import com.gravifox.domain.admin.repository.TermRepository;
import com.gravifox.domain.admin.service.impl.TermServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
public class TermServiceTest {
    @Mock
    private TermRepository termRepository;

    @InjectMocks
    private TermServiceImpl service;

    @Test
    @DisplayName("**NewTerm DTO를 이용해서 약관 엔터티를 생성 후 저장한다.")
    void createNewTerm_returnVoid() {
        NewTerm newTerm = NewTerm.builder()
                .termKey("tos")
                .termTitle("Terms of Service")
                .termVersion("1.0")
                .termContent("서비스 이용약관 내용입니다.")
                .isRequired(true)
                .effectiveAt(LocalDateTime.now())
                .build();

        String termTitle = service.createTerm(newTerm);

        assertThat(newTerm.getTermTitle()).isEqualTo(termTitle);
    }
}
