package com.finup.asset;

import com.finup.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService service;

    @GetMapping
    public ResponseEntity<List<AssetDto>> list(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(service.listByUser(user, activeOnly));
    }

    @PostMapping
    public ResponseEntity<AssetDto> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AssetRequest req) {
        return ResponseEntity.status(201).body(service.create(user, req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetDto> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody AssetRequest req) {
        return ResponseEntity.ok(service.update(user, id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        service.deactivate(user, id);
        return ResponseEntity.noContent().build();
    }
}
