package com.finup.entry;

import com.finup.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entries")
@RequiredArgsConstructor
public class EntryController {

    private final EntryService service;

    @GetMapping
    public ResponseEntity<List<EntryDto>> getByPeriod(
            @AuthenticationPrincipal User user,
            @RequestParam short year,
            @RequestParam short month) {
        return ResponseEntity.ok(service.getByPeriod(user, year, month));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<EntryDto>> saveBatch(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody EntryBatchRequest req) {
        return ResponseEntity.ok(service.saveBatch(user, req));
    }
}
