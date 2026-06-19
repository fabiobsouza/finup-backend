package com.finup.dashboard;

import com.finup.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyShort;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @InjectMocks
    private DashboardController dashboardController;

    @Mock
    private DashboardService dashboardService;

    @Test
    void testSummary() {
        User user = User.builder().id(1L).build();
        DashboardSummaryDto dto = new DashboardSummaryDto(
                (short) 2023, (short) 10, BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN, BigDecimal.ZERO,
                List.of(), List.of(), List.of()
        );

        when(dashboardService.getSummary(eq(user), anyShort(), anyShort(), anyInt())).thenReturn(dto);

        ResponseEntity<DashboardSummaryDto> response = dashboardController.summary(user, (short) 2023, (short) 10, 12);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals((short) 2023, response.getBody().year());
    }
}
