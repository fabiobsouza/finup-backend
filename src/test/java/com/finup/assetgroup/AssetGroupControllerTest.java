package com.finup.assetgroup;

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
class AssetGroupControllerTest {

    @InjectMocks
    private AssetGroupController assetGroupController;

    @Mock
    private AssetGroupService assetGroupService;

    @Test
    void testList() {
        User user = User.builder().id(1L).build();
        AssetGroupDto dto = new AssetGroupDto(10L, "Group", 0, 1, null);
        when(assetGroupService.listByUser(user)).thenReturn(List.of(dto));

        ResponseEntity<List<AssetGroupDto>> response = assetGroupController.list(user);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(1, response.getBody().size());
        assertEquals(10L, response.getBody().get(0).id());
    }

    @Test
    void testCreate() {
        User user = User.builder().id(1L).build();
        AssetGroupRequest req = new AssetGroupRequest("New Group", 1);
        AssetGroupDto dto = new AssetGroupDto(20L, "New Group", 1, 0, null);
        
        when(assetGroupService.create(user, req)).thenReturn(dto);

        ResponseEntity<AssetGroupDto> response = assetGroupController.create(user, req);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(20L, response.getBody().id());
    }

    @Test
    void testUpdate() {
        User user = User.builder().id(1L).build();
        AssetGroupRequest req = new AssetGroupRequest("Updated Group", 2);
        AssetGroupDto dto = new AssetGroupDto(10L, "Updated Group", 2, 0, null);

        when(assetGroupService.update(user, 10L, req)).thenReturn(dto);

        ResponseEntity<AssetGroupDto> response = assetGroupController.update(user, 10L, req);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Updated Group", response.getBody().name());
    }

    @Test
    void testDelete() {
        User user = User.builder().id(1L).build();

        ResponseEntity<Void> response = assetGroupController.delete(user, 10L);

        assertEquals(204, response.getStatusCodeValue());
        verify(assetGroupService).delete(user, 10L);
    }
}
