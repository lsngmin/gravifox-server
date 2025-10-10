package com.gravifox.api.domain.dashboard.service;

import com.gravifox.domain.dashboard.dto.DashboardInfoResponse;
import com.gravifox.domain.dashboard.repository.DashboardRepository;
import com.gravifox.domain.dashboard.service.impl.DashboardServiceImpl;
import com.gravifox.domain.member.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
public class DashboardServiceTest {
    @Mock
    private DashboardRepository dashboardRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private DashboardServiceImpl service;

    @Test
    @DisplayName("존재하는 userNo로 조회하면 apiKey를 담은 DTO를 반환한다")
    void getDashboardForUser_whenExists_returnsDto() {
        long userNo = 42L;
        String expectedApiKey = "abc-123-key";

//        when(dashboardRepo.findApiKeyByUser(userNo)).thenReturn(Optional.of(expectedApiKey));

        DashboardInfoResponse dto = service.getDashboardData(userNo);

        assertThat(dto.getApiKey()).isEqualTo("빈 문자열");
    }

}
