package com.finup.assetgroup;

import com.finup.asset.Asset;
import com.finup.asset.AssetRepository;
import com.finup.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetGroupServiceTest {

    @InjectMocks
    private AssetGroupService assetGroupService;

    @Mock
    private AssetGroupRepository assetGroupRepository;

    @Mock
    private AssetRepository assetRepository;

    @Test
    void testListByUser() {
        User user = User.builder().id(1L).build();
        AssetGroup group = AssetGroup.builder().id(10L).name("Group 1").build();
        
        Asset asset1 = Asset.builder().id(100L).active(true).build();
        Asset asset2 = Asset.builder().id(101L).active(false).build();

        when(assetGroupRepository.findByUserOrderByDisplayOrderAscNameAsc(user))
                .thenReturn(List.of(group));
        
        when(assetRepository.findByGroupAndUserOrderByDisplayOrderAscNameAsc(group, user))
                .thenReturn(List.of(asset1, asset2));

        List<AssetGroupDto> result = assetGroupService.listByUser(user);

        assertEquals(1, result.size());
        assertEquals("Group 1", result.get(0).name());
        assertEquals(1, result.get(0).assetCount()); // only 1 active
    }

    @Test
    void testCreate() {
        User user = User.builder().id(1L).build();
        AssetGroupRequest req = new AssetGroupRequest("New Group", 5);

        when(assetGroupRepository.save(any(AssetGroup.class))).thenAnswer(invocation -> {
            AssetGroup saved = invocation.getArgument(0);
            saved.setId(20L);
            return saved;
        });

        AssetGroupDto result = assetGroupService.create(user, req);

        assertNotNull(result);
        assertEquals(20L, result.id());
        assertEquals("New Group", result.name());
        assertEquals(5, result.displayOrder());
    }

    @Test
    void testUpdate_Success() {
        User user = User.builder().id(1L).build();
        AssetGroup existing = AssetGroup.builder().id(10L).name("Old Name").displayOrder(0).build();
        
        AssetGroupRequest req = new AssetGroupRequest("Updated Name", 10);

        when(assetGroupRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(existing));
        when(assetGroupRepository.save(any(AssetGroup.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssetGroupDto result = assetGroupService.update(user, 10L, req);

        assertEquals("Updated Name", result.name());
        assertEquals(10, result.displayOrder());
    }

    @Test
    void testUpdate_NotFound() {
        User user = User.builder().id(1L).build();
        AssetGroupRequest req = new AssetGroupRequest("Updated Name", 10);

        when(assetGroupRepository.findByIdAndUser(10L, user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> assetGroupService.update(user, 10L, req));
    }

    @Test
    void testDelete_Success() {
        User user = User.builder().id(1L).build();
        AssetGroup existing = AssetGroup.builder().id(10L).build();

        when(assetGroupRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(existing));

        assetGroupService.delete(user, 10L);

        verify(assetGroupRepository).delete(existing);
    }

    @Test
    void testDelete_NotFound() {
        User user = User.builder().id(1L).build();

        when(assetGroupRepository.findByIdAndUser(10L, user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> assetGroupService.delete(user, 10L));
    }
}
