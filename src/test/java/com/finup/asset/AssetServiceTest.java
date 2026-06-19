package com.finup.asset;

import com.finup.assetgroup.AssetGroup;
import com.finup.assetgroup.AssetGroupRepository;
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
class AssetServiceTest {

    @InjectMocks
    private AssetService assetService;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetGroupRepository groupRepository;

    @Test
    void testListByUser_ActiveOnly() {
        User user = User.builder().id(1L).build();
        AssetGroup group = AssetGroup.builder().id(10L).name("Group").build();
        Asset asset1 = Asset.builder().id(100L).group(group).active(true).build();
        
        when(assetRepository.findByUserWithGroup(user, Boolean.TRUE)).thenReturn(List.of(asset1));

        List<AssetDto> result = assetService.listByUser(user, true);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).id());
    }

    @Test
    void testListByUser_All() {
        User user = User.builder().id(1L).build();
        AssetGroup group = AssetGroup.builder().id(10L).name("Group").build();
        Asset asset1 = Asset.builder().id(100L).group(group).active(true).build();
        
        when(assetRepository.findByUserWithGroup(user, null)).thenReturn(List.of(asset1));

        List<AssetDto> result = assetService.listByUser(user, false);

        assertEquals(1, result.size());
    }

    @Test
    void testCreate_Success() {
        User user = User.builder().id(1L).build();
        AssetRequest req = new AssetRequest(10L, "Asset 1", "Inst 1", "Desc", 5);
        AssetGroup group = AssetGroup.builder().id(10L).name("Group").build();

        when(groupRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(group));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> {
            Asset saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        AssetDto result = assetService.create(user, req);

        assertNotNull(result);
        assertEquals(100L, result.id());
        assertEquals("Asset 1", result.name());
        assertEquals("Inst 1", result.institution());
    }

    @Test
    void testCreate_GroupNotFound() {
        User user = User.builder().id(1L).build();
        AssetRequest req = new AssetRequest(10L, "Asset 1", "Inst 1", "Desc", 5);

        when(groupRepository.findByIdAndUser(10L, user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> assetService.create(user, req));
    }

    @Test
    void testUpdate_Success() {
        User user = User.builder().id(1L).build();
        AssetGroup oldGroup = AssetGroup.builder().id(10L).name("Group 1").build();
        AssetGroup newGroup = AssetGroup.builder().id(11L).name("Group 2").build();
        
        Asset existingAsset = Asset.builder().id(100L).group(oldGroup).name("Old").displayOrder(0).build();
        AssetRequest req = new AssetRequest(11L, "Updated Asset", "Inst 2", "Desc 2", 10);

        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(existingAsset));
        when(groupRepository.findByIdAndUser(11L, user)).thenReturn(Optional.of(newGroup));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssetDto result = assetService.update(user, 100L, req);

        assertEquals("Updated Asset", result.name());
        assertEquals(11L, result.groupId());
        assertEquals(10, result.displayOrder());
    }

    @Test
    void testUpdate_AssetNotFound() {
        User user = User.builder().id(1L).build();
        AssetRequest req = new AssetRequest(11L, "Updated Asset", "Inst 2", "Desc 2", 10);

        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> assetService.update(user, 100L, req));
    }

    @Test
    void testUpdate_GroupNotFound() {
        User user = User.builder().id(1L).build();
        AssetGroup oldGroup = AssetGroup.builder().id(10L).name("Group 1").build();
        Asset existingAsset = Asset.builder().id(100L).group(oldGroup).name("Old").displayOrder(0).build();
        AssetRequest req = new AssetRequest(11L, "Updated Asset", "Inst 2", "Desc 2", 10);

        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(existingAsset));
        when(groupRepository.findByIdAndUser(11L, user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> assetService.update(user, 100L, req));
    }

    @Test
    void testDeactivate_Success() {
        User user = User.builder().id(1L).build();
        Asset existingAsset = Asset.builder().id(100L).active(true).build();

        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(existingAsset));

        assetService.deactivate(user, 100L);

        verify(assetRepository).save(existingAsset);
        assertFalse(existingAsset.getActive());
    }

    @Test
    void testDeactivate_NotFound() {
        User user = User.builder().id(1L).build();

        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> assetService.deactivate(user, 100L));
    }
}
