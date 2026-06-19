package com.finup.asset;

import com.finup.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetControllerTest {

    @InjectMocks
    private AssetController assetController;

    @Mock
    private AssetService assetService;

    @Test
    void testList() {
        User user = User.builder().id(1L).build();
        AssetDto dto = new AssetDto(100L, 10L, "Group", "Asset", "Inst", "Desc", true, 0);
        when(assetService.listByUser(user, true)).thenReturn(List.of(dto));

        ResponseEntity<List<AssetDto>> response = assetController.list(user, true);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(100L, response.getBody().get(0).id());
    }

    @Test
    void testCreate() {
        User user = User.builder().id(1L).build();
        AssetRequest req = new AssetRequest(10L, "Asset", "Inst", "Desc", 0);
        AssetDto dto = new AssetDto(100L, 10L, "Group", "Asset", "Inst", "Desc", true, 0);
        
        when(assetService.create(user, req)).thenReturn(dto);

        ResponseEntity<AssetDto> response = assetController.create(user, req);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(100L, response.getBody().id());
    }

    @Test
    void testUpdate() {
        User user = User.builder().id(1L).build();
        AssetRequest req = new AssetRequest(10L, "Updated Asset", "Inst", "Desc", 0);
        AssetDto dto = new AssetDto(100L, 10L, "Group", "Updated Asset", "Inst", "Desc", true, 0);

        when(assetService.update(user, 100L, req)).thenReturn(dto);

        ResponseEntity<AssetDto> response = assetController.update(user, 100L, req);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Updated Asset", response.getBody().name());
    }

    @Test
    void testDeactivate() {
        User user = User.builder().id(1L).build();

        ResponseEntity<Void> response = assetController.deactivate(user, 100L);

        assertEquals(204, response.getStatusCodeValue());
        verify(assetService).deactivate(user, 100L);
    }
}
