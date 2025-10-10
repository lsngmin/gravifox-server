package com.gravifox.api.domain.dashboard.repo;

import com.gravifox.domain.dashboard.domain.Dashboard;
import com.gravifox.domain.dashboard.repository.DashboardRepository;
import com.gravifox.domain.member.domain.user.LoginType;
import com.gravifox.domain.member.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DashboardRepoTest {
    @Autowired
    DashboardRepository dashboardRepository;

    @Autowired
    TestEntityManager entityManager;

    private User u;
    private Dashboard d;

    @BeforeEach
    void init() {
         u = User.builder()
                .userId("TEST!@#123")
                .loginType(LoginType.EMAIL)
                .build();
        entityManager.persist(u);

        d = Dashboard.builder()
                .apiKey("API키 입니다.")
                .user(u)
                .build();
        entityManager.persist(d);

        entityManager.flush();
    }

    @Test
    void findByUserNo_returnsDashboard() {
        Optional<String> foundApiKey = dashboardRepository.findApiKeyByUser(u.getUserNo());
        assertThat(foundApiKey).isPresent();
    }

    @Test
    @DisplayName("Dashboard 테이블에 유저 정보가 없을 경우")
    void updateApikey_whenNotExist_shouldCreateDashboard() {
        Optional<Dashboard> foundDashboard = dashboardRepository.findDashboardByUserNo(u.getUserNo());
        assertThat(foundDashboard).isPresent();
    }

}
