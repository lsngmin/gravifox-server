package com.gravifox.api.domain.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gravifox.domain.admin.dto.NewTerm;
import com.gravifox.domain.member.domain.user.LoginType;
import com.gravifox.domain.member.domain.user.User;
import com.gravifox.domain.member.repository.UserRepository;
import com.gravifox.security.jwt.util.JWTUtil;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AdminTermControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JWTUtil jwtUtil;

    private User savedUser;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .loginType(LoginType.EMAIL)
                .userId("test123@example.com")
                .build();
        savedUser = userRepository.save(user);
    }
    @Test
    @DisplayName("**[POST]/admin/v1/term ** 새로운 약관을 등록할 수 있다.")
    void postTerm() throws Exception {
        String token = jwtUtil.createToken(Map.of("userId", "test@example.com",
                "userNo", String.valueOf(savedUser.getUserNo())), 10);

        NewTerm newTerm = NewTerm.builder()
                .termKey("tos")
                .termTitle("Terms of Service")
                .termVersion("1.0")
                .termContent("서비스 이용약관 내용입니다.")
                .isRequired(true)
                .effectiveAt(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/admin/v1/terms")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTerm))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.equalTo(newTerm.getTermTitle())));
    }
}
