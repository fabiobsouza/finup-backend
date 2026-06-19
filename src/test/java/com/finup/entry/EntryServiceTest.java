package com.finup.entry;

import com.finup.asset.Asset;
import com.finup.asset.AssetRepository;
import com.finup.assetgroup.AssetGroup;
import com.finup.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntryServiceTest {

    @InjectMocks
    private EntryService entryService;

    @Mock
    private MonthlyEntryRepository entryRepository;

    @Mock
    private AssetRepository assetRepository;

    @Test
    void testGetByPeriod() {
        User user = User.builder().id(1L).build();
        AssetGroup group = AssetGroup.builder().id(10L).name("Group").build();
        Asset asset = Asset.builder().id(100L).name("Asset").group(group).build();
        MonthlyEntry entry = MonthlyEntry.builder().id(1000L).asset(asset).year((short) 2023).month((short) 10).balance(BigDecimal.TEN).build();

        when(entryRepository.findByUserAndYearAndMonthFetch(1L, 2023, 10)).thenReturn(List.of(entry));

        List<EntryDto> result = entryService.getByPeriod(user, (short) 2023, (short) 10);

        assertEquals(1, result.size());
        assertEquals(1000L, result.get(0).id());
        assertEquals(BigDecimal.TEN, result.get(0).balance());
    }

    @Test
    void testSaveBatch_ExistingEntry() {
        User user = User.builder().id(1L).build();
        AssetGroup group = AssetGroup.builder().id(10L).name("Group").build();
        Asset asset = Asset.builder().id(100L).name("Asset").group(group).build();
        MonthlyEntry existingEntry = MonthlyEntry.builder().id(1000L).asset(asset).year((short) 2023).month((short) 10).balance(BigDecimal.ZERO).build();

        EntryBatchRequest.EntryItem item = new EntryBatchRequest.EntryItem(100L, BigDecimal.TEN, "Notes");
        EntryBatchRequest req = new EntryBatchRequest((short) 2023, (short) 10, List.of(item));

        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(asset));
        when(entryRepository.findByAssetAndYearAndMonth(asset, (short) 2023, (short) 10)).thenReturn(Optional.of(existingEntry));
        when(entryRepository.save(any(MonthlyEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<EntryDto> result = entryService.saveBatch(user, req);

        assertEquals(1, result.size());
        assertEquals(1000L, result.get(0).id());
        assertEquals(BigDecimal.TEN, result.get(0).balance());
        assertEquals("Notes", result.get(0).notes());
    }

    @Test
    void testSaveBatch_NewEntry() {
        User user = User.builder().id(1L).build();
        AssetGroup group = AssetGroup.builder().id(10L).name("Group").build();
        Asset asset = Asset.builder().id(100L).name("Asset").group(group).build();

        EntryBatchRequest.EntryItem item = new EntryBatchRequest.EntryItem(100L, BigDecimal.TEN, "Notes");
        EntryBatchRequest req = new EntryBatchRequest((short) 2023, (short) 10, List.of(item));

        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(asset));
        when(entryRepository.findByAssetAndYearAndMonth(asset, (short) 2023, (short) 10)).thenReturn(Optional.empty());
        when(entryRepository.save(any(MonthlyEntry.class))).thenAnswer(invocation -> {
            MonthlyEntry saved = invocation.getArgument(0);
            saved.setId(2000L);
            return saved;
        });

        List<EntryDto> result = entryService.saveBatch(user, req);

        assertEquals(1, result.size());
        assertEquals(2000L, result.get(0).id());
        assertEquals(BigDecimal.TEN, result.get(0).balance());
    }

    @Test
    void testSaveBatch_AssetNotFound() {
        User user = User.builder().id(1L).build();

        EntryBatchRequest.EntryItem item = new EntryBatchRequest.EntryItem(100L, BigDecimal.TEN, "Notes");
        EntryBatchRequest req = new EntryBatchRequest((short) 2023, (short) 10, List.of(item));

        when(assetRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> entryService.saveBatch(user, req));
    }
}
