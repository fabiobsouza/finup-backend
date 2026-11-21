package com.finup.report;

import com.finup.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class AnnualReportController {

    private final AnnualReportService service;

    @GetMapping("/annual")
    public ResponseEntity<AnnualReportDto> annual(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(service.getAnnual(user, year));
    }
}
