package com.finup.asset;

import com.finup.assetgroup.AssetGroup;
import com.finup.assetgroup.AssetGroupRepository;
import com.finup.entry.MonthlyEntry;
import com.finup.entry.MonthlyEntryRepository;
import com.finup.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    /** "Hoje" nos testes: 15/11/2026. */
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-15T12:00:00Z"), ZoneId.of("UTC"));

    private AssetService assetService;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetGroupRepository groupRepository;

    @Mock
    private MonthlyEntryRepository entryRepository;

    private final User user = User.builder().id(1L).build();
    private final AssetGroup group = AssetGroup.builder().id(10L).name("Group").build();

    @BeforeEach
    void setUp() {
        assetService = new AssetService(assetRepository, groupRepository, entryRepository, CLOCK);
    }

    private Asset asset() {
        return Asset.builder().id(100L).name("CDI").group(group).build();
    }

    private static MonthlyEntry entry(int year, int month) {
        return MonthlyEntry.builder().year((short) year).month((short) month).build();
    }

    // ── list ────────────────────────────────────────────────────────────────

    @Test
    void testListByUser_ExcludesLiquidatedByDefault() {
        when(assetRepository.findByUserWithGroup(user, false)).thenReturn(List.of(asset()));

        List<AssetDto> result = assetService.listByUser(user, false);

        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).id());
        assertNull(result.get(0).liquidatedAt());
    }

    @Test
    void testListByUser_IncludeLiquidated() {
        Asset liquidated = asset();
        liquidated.setLiquidatedAt(LocalDate.of(2026, 3, 1));
        when(assetRepository.findByUserWithGroup(user, true)).thenReturn(List.of(liquidated));

        List<AssetDto> result = assetService.listByUser(user, true);

        assertEquals(LocalDate.of(2026, 3, 1), result.get(0).liquidatedAt());
    }

    // ── create / update ─────────────────────────────────────────────────────

    @Test
    void testCreate_Success() {
        AssetRequest req = new AssetRequest(10L, "Asset 1", "Inst 1", "Desc", 5);

        when(groupRepository.findByIdAndUser(10L, user)).thenReturn(Optional.of(group));
        when(assetRepository.save(any(Asset.class))).thenAnswer(invocation -> {
            Asset saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        AssetDto result = assetService.create(user, req);

        assertEquals(100L, result.id());
        assertEquals("Asset 1", result.name());
        assertEquals("Inst 1", result.institution());
        assertNull(result.liquidatedAt());
    }

    @Test
    void testCreate_GroupNotFound() {
        AssetRequest req = new AssetRequest(10L, "Asset 1", "Inst 1", "Desc", 5);
        when(groupRepository.findByIdAndUser(10L, user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> assetService.create(user, req));
    }

    @Test
    void testUpdate_Success() {
        AssetGroup newGroup = AssetGroup.builder().id(11L).name("Group 2").build();
        Asset existingAsset = Asset.builder().id(100L).group(group).name("Old").displayOrder(0).build();
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
        AssetRequest req = new AssetRequest(11L, "Updated Asset", "Inst 2", "Desc 2", 10);
        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> assetService.update(user, 100L, req));
    }

    @Test
    void testUpdate_GroupNotFound() {
        AssetRequest req = new AssetRequest(11L, "Updated Asset", "Inst 2", "Desc 2", 10);
        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(asset()));
        when(groupRepository.findByIdAndUser(11L, user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> assetService.update(user, 100L, req));
    }

    // ── liquidate ───────────────────────────────────────────────────────────

    @Test
    void testLiquidate_AfterLastEntry() {
        Asset asset = asset();
        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(asset));
        when(entryRepository.findFirstByAssetOrderByYearDescMonthDesc(asset)).thenReturn(Optional.of(entry(2026, 9)));
        when(assetRepository.save(asset)).thenReturn(asset);

        AssetDto result = assetService.liquidate(user, 100L, new LiquidationRequest(2026, 10));

        assertEquals(LocalDate.of(2026, 10, 1), result.liquidatedAt());
    }

    @Test
    void testLiquidate_WithoutEntries() {
        Asset asset = asset();
        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(asset));
        when(entryRepository.findFirstByAssetOrderByYearDescMonthDesc(asset)).thenReturn(Optional.empty());
        when(assetRepository.save(asset)).thenReturn(asset);

        assetService.liquidate(user, 100L, new LiquidationRequest(2025, 1));

        assertEquals(LocalDate.of(2025, 1, 1), asset.getLiquidatedAt());
    }

    @Test
    void testLiquidate_NextMonthIsAllowed() {
        Asset asset = asset();
        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(asset));
        when(entryRepository.findFirstByAssetOrderByYearDescMonthDesc(asset)).thenReturn(Optional.of(entry(2026, 11)));
        when(assetRepository.save(asset)).thenReturn(asset);

        assetService.liquidate(user, 100L, new LiquidationRequest(2026, 12));

        assertEquals(LocalDate.of(2026, 12, 1), asset.getLiquidatedAt());
    }

    @Test
    void testLiquidate_RejectsMonthTooFarInFuture() {
        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(asset()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> assetService.liquidate(user, 100L, new LiquidationRequest(2027, 1)));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(assetRepository, never()).save(any());
    }

    @Test
    void testLiquidate_RejectsWhenEntryExistsAfterMonth() {
        Asset asset = asset();
        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(asset));
        when(entryRepository.findFirstByAssetOrderByYearDescMonthDesc(asset)).thenReturn(Optional.of(entry(2026, 10)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> assetService.liquidate(user, 100L, new LiquidationRequest(2026, 8)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        assertTrue(ex.getReason().contains("10/2026"));
        assertTrue(ex.getReason().contains("11/2026"), "deve sugerir o mês seguinte ao último lançamento");
        assertNull(asset.getLiquidatedAt());
    }

    @Test
    void testLiquidate_RejectsSameMonthAsLastEntry() {
        Asset asset = asset();
        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(asset));
        when(entryRepository.findFirstByAssetOrderByYearDescMonthDesc(asset)).thenReturn(Optional.of(entry(2026, 10)));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> assetService.liquidate(user, 100L, new LiquidationRequest(2026, 10)));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }

    @Test
    void testLiquidate_NotFound() {
        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> assetService.liquidate(user, 100L, new LiquidationRequest(2026, 10)));
    }

    @Test
    void testUndoLiquidation() {
        Asset asset = asset();
        asset.setLiquidatedAt(LocalDate.of(2026, 5, 1));
        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(asset));
        when(assetRepository.save(asset)).thenReturn(asset);

        AssetDto result = assetService.undoLiquidation(user, 100L);

        assertNull(result.liquidatedAt());
    }

    // ── delete ──────────────────────────────────────────────────────────────

    @Test
    void testDelete_WithoutEntries() {
        Asset asset = asset();
        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(asset));
        when(entryRepository.existsByAsset(asset)).thenReturn(false);

        assetService.delete(user, 100L);

        verify(assetRepository).delete(asset);
    }

    @Test
    void testDelete_RejectsWhenAssetHasEntries() {
        Asset asset = asset();
        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(asset));
        when(entryRepository.existsByAsset(asset)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> assetService.delete(user, 100L));

        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
        verify(assetRepository, never()).delete(any());
    }

    @Test
    void testDelete_NotFound() {
        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> assetService.delete(user, 100L));
    }
}
