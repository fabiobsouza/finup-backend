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
            @RequestParam(defaultValue = "false") boolean includeLiquidated) {
        return ResponseEntity.ok(service.listByUser(user, includeLiquidated));
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

    @PostMapping("/{id}/liquidation")
    public ResponseEntity<AssetDto> liquidate(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody LiquidationRequest req) {
        return ResponseEntity.ok(service.liquidate(user, id, req));
    }

    @DeleteMapping("/{id}/liquidation")
    public ResponseEntity<AssetDto> undoLiquidation(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        return ResponseEntity.ok(service.undoLiquidation(user, id));
    }

    /** Exclusão física; recusada (409) se o ativo tiver lançamentos. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User user,
            @PathVariable Long id) {
        service.delete(user, id);
        return ResponseEntity.noContent().build();
    }
}
