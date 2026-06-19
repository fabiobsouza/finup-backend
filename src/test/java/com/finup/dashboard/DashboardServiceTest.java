package com.finup.dashboard;

import com.finup.asset.Asset;
import com.finup.assetgroup.AssetGroup;
import com.finup.entry.MonthlyEntry;
import com.finup.entry.MonthlyEntryRepository;
import com.finup.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @InjectMocks
    private DashboardService dashboardService;

    @Mock
    private MonthlyEntryRepository entryRepository;

    @Test
    void testGetSummary() {
        User user = User.builder().id(1L).build();
        AssetGroup group = AssetGroup.builder().id(10L).name("Group 1").build();
        Asset asset1 = Asset.builder().id(100L).name("Asset 1").institution("Inst 1").group(group).build();
        Asset asset2 = Asset.builder().id(101L).name("Asset 2").institution("Inst 2").group(group).build();

        // Current month entries (e.g. 2023-10)
        MonthlyEntry entry1 = MonthlyEntry.builder().year((short) 2023).month((short) 10).asset(asset1).balance(new BigDecimal("100.00")).build();
        MonthlyEntry entry2 = MonthlyEntry.builder().year((short) 2023).month((short) 10).asset(asset2).balance(new BigDecimal("200.00")).build();
        
        // Previous month entry (2023-09)
        MonthlyEntry prevEntry = MonthlyEntry.builder().year((short) 2023).month((short) 9).asset(asset1).balance(new BigDecimal("150.00")).build();

        when(entryRepository.findByUserFromPeriod(eq(1L), anyInt(), anyInt()))
                .thenReturn(List.of(entry1, entry2, prevEntry));

        DashboardSummaryDto summary = dashboardService.getSummary(user, (short) 2023, (short) 10, 3);

        assertEquals((short) 2023, summary.year());
        assertEquals((short) 10, summary.month());

        // Total for current month: 100 + 200 = 300
        assertEquals(0, new BigDecimal("300.00").compareTo(summary.totalBalance()));

        // Total for prev month: 150
        assertEquals(0, new BigDecimal("150.00").compareTo(summary.previousBalance()));

        // Variation: 300 - 150 = +150
        assertEquals(0, new BigDecimal("150.00").compareTo(summary.variation()));

        // Variation %: (150 / 150) * 100 = 100%
        assertEquals(0, new BigDecimal("100.00").compareTo(summary.variationPercent()));

        // Asset slices
        assertEquals(2, summary.assetDistribution().size());
        
        // Group slices
        assertEquals(1, summary.groupDistribution().size());
        assertEquals("Group 1", summary.groupDistribution().get(0).groupName());
        assertEquals(0, new BigDecimal("300.00").compareTo(summary.groupDistribution().get(0).total()));
        assertEquals(0, new BigDecimal("100.00").compareTo(summary.groupDistribution().get(0).percent()));
    }

    @Test
    void testGetSummary_EmptyHistory() {
        User user = User.builder().id(1L).build();
        when(entryRepository.findByUserFromPeriod(eq(1L), anyInt(), anyInt())).thenReturn(List.of());

        DashboardSummaryDto summary = dashboardService.getSummary(user, (short) 2023, (short) 10, 3);

        assertEquals(0, BigDecimal.ZERO.compareTo(summary.totalBalance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.previousBalance()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.variation()));
        assertEquals(0, BigDecimal.ZERO.compareTo(summary.variationPercent()));
        assertEquals(0, summary.assetDistribution().size());
        assertEquals(0, summary.groupDistribution().size());
    }
}
