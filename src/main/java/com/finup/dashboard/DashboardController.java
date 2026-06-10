package com.finup.dashboard;

import com.finup.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDto> summary(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Short year,
            @RequestParam(required = false) Short month,
            @RequestParam(defaultValue = "24") int months) {

        LocalDate now = LocalDate.now();
        short y = year  != null ? year  : (short) now.getYear();
        short m = month != null ? month : (short) now.getMonthValue();

        return ResponseEntity.ok(service.getSummary(user, y, m, months));
    }
}
