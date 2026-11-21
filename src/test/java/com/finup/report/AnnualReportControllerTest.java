package com.finup.report;

import com.finup.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnualReportControllerTest {

    @InjectMocks
    private AnnualReportController controller;

    @Mock
    private AnnualReportService service;

    @Test
    void testAnnual() {
        User user = User.builder().id(1L).build();
        AnnualReportDto dto = new AnnualReportDto(2026, List.of(2026), List.of(), null);
        when(service.getAnnual(user, 2026)).thenReturn(dto);

        ResponseEntity<AnnualReportDto> response = controller.annual(user, 2026);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2026, response.getBody().year());
    }
}
