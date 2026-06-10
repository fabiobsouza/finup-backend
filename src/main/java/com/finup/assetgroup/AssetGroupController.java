package com.finup.assetgroup;

import com.finup.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asset-groups")
@RequiredArgsConstructor
public class AssetGroupController {

    private final AssetGroupService service;

    @GetMapping
    public ResponseEntity<List<AssetGroupDto>> list(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.listByUser(user));
    }

    @PostMapping
    public ResponseEntity<AssetGroupDto> create(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AssetGroupRequest req) {
        return ResponseEntity.status(201).body(service.create(user, req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetGroupDto> update(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody AssetGroupRequest req) {
        return ResponseEntity.ok(service.update(user, id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        service.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
