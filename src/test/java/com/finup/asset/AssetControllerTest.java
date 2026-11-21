package com.finup.asset;

import com.finup.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetControllerTest {

    @InjectMocks
    private AssetController assetController;

    @Mock
    private AssetService assetService;

    private final User user = User.builder().id(1L).build();

    private static AssetDto dto(String name, LocalDate liquidatedAt) {
        return new AssetDto(100L, 10L, "Group", name, "Inst", "Desc", liquidatedAt, 0);
    }

    @Test
    void testList() {
        when(assetService.listByUser(user, true)).thenReturn(List.of(dto("Asset", null)));

        ResponseEntity<List<AssetDto>> response = assetController.list(user, true);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(100L, response.getBody().get(0).id());
    }

    @Test
    void testCreate() {
        AssetRequest req = new AssetRequest(10L, "Asset", "Inst", "Desc", 0);
        when(assetService.create(user, req)).thenReturn(dto("Asset", null));

        ResponseEntity<AssetDto> response = assetController.create(user, req);

        assertEquals(201, response.getStatusCode().value());
        assertEquals(100L, response.getBody().id());
    }

    @Test
    void testUpdate() {
        AssetRequest req = new AssetRequest(10L, "Updated Asset", "Inst", "Desc", 0);
        when(assetService.update(user, 100L, req)).thenReturn(dto("Updated Asset", null));

        ResponseEntity<AssetDto> response = assetController.update(user, 100L, req);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Updated Asset", response.getBody().name());
    }

    @Test
    void testLiquidate() {
        LiquidationRequest req = new LiquidationRequest(2026, 10);
        when(assetService.liquidate(user, 100L, req)).thenReturn(dto("Asset", LocalDate.of(2026, 10, 1)));

        ResponseEntity<AssetDto> response = assetController.liquidate(user, 100L, req);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(LocalDate.of(2026, 10, 1), response.getBody().liquidatedAt());
    }

    @Test
    void testUndoLiquidation() {
        when(assetService.undoLiquidation(user, 100L)).thenReturn(dto("Asset", null));

        ResponseEntity<AssetDto> response = assetController.undoLiquidation(user, 100L);

        assertEquals(200, response.getStatusCode().value());
        verify(assetService).undoLiquidation(user, 100L);
    }

    @Test
    void testDelete() {
        ResponseEntity<Void> response = assetController.delete(user, 100L);

        assertEquals(204, response.getStatusCode().value());
        verify(assetService).delete(user, 100L);
    }
}
