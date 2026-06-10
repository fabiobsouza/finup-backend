package com.finup.dashboard;

import com.finup.entry.MonthlyEntry;
import com.finup.entry.MonthlyEntryRepository;
import com.finup.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MonthlyEntryRepository entryRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary(User user, short year, short month, int historyMonths) {
        // --- History window ---
        YearMonth current  = YearMonth.of(year, month);
        YearMonth fromMonth = current.minusMonths(historyMonths - 1L);
        short fromYear  = (short) fromMonth.getYear();
        short fromMonthN = (short) fromMonth.getMonthValue();

        List<MonthlyEntry> allEntries = entryRepository.findByUserFromPeriod(
                user.getId(), (int) fromYear, (int) fromMonthN);

        // Group by period
        Map<YearMonth, List<MonthlyEntry>> byPeriod = allEntries.stream()
                .collect(Collectors.groupingBy(e -> YearMonth.of(e.getYear(), e.getMonth())));

        // Build history list (one point per month in window)
        List<DashboardSummaryDto.PeriodPoint> history = new ArrayList<>();
        for (int i = historyMonths - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            List<MonthlyEntry> periodEntries = byPeriod.getOrDefault(ym, List.of());
            BigDecimal total = periodEntries.stream()
                    .map(MonthlyEntry::getBalance)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            String label = ym.getMonth()
                    .getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")) + "/" +
                    String.valueOf(ym.getYear()).substring(2);

            history.add(new DashboardSummaryDto.PeriodPoint(
                    (short) ym.getYear(), (short) ym.getMonthValue(), label, total));
        }

        // Current period totals
        BigDecimal totalBalance = history.isEmpty() ? BigDecimal.ZERO
                : history.get(history.size() - 1).total();

        // Previous period
        YearMonth prevYm = current.minusMonths(1);
        BigDecimal previousBalance = history.stream()
                .filter(p -> p.year() == (short) prevYm.getYear() && p.month() == (short) prevYm.getMonthValue())
                .map(DashboardSummaryDto.PeriodPoint::total)
                .findFirst()
                .orElse(BigDecimal.ZERO);

        BigDecimal variation = totalBalance.subtract(previousBalance);
        BigDecimal variationPercent = previousBalance.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : variation.divide(previousBalance, 4, RoundingMode.HALF_UP)
                           .multiply(BigDecimal.valueOf(100))
                           .setScale(2, RoundingMode.HALF_UP);

        // Group distribution for current period
        List<MonthlyEntry> currentEntries = byPeriod.getOrDefault(current, List.of());
        Map<Long, List<MonthlyEntry>> byGroup = currentEntries.stream()
                .collect(Collectors.groupingBy(e -> e.getAsset().getGroup().getId()));

        List<DashboardSummaryDto.GroupSlice> groupDistribution = byGroup.entrySet().stream()
                .map(entry -> {
                    BigDecimal groupTotal = entry.getValue().stream()
                            .map(MonthlyEntry::getBalance)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal pct = totalBalance.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : groupTotal.divide(totalBalance, 4, RoundingMode.HALF_UP)
                                        .multiply(BigDecimal.valueOf(100))
                                        .setScale(2, RoundingMode.HALF_UP);
                    String groupName = entry.getValue().get(0).getAsset().getGroup().getName();
                    return new DashboardSummaryDto.GroupSlice(entry.getKey(), groupName, groupTotal, pct);
                })
                .sorted(Comparator.comparing(DashboardSummaryDto.GroupSlice::total).reversed())
                .toList();

        // Asset distribution — per-asset detail, sorted by balance desc
        List<DashboardSummaryDto.AssetSlice> assetDistribution = currentEntries.stream()
                .map(e -> {
                    BigDecimal pct = totalBalance.compareTo(BigDecimal.ZERO) == 0
                            ? BigDecimal.ZERO
                            : e.getBalance().divide(totalBalance, 4, RoundingMode.HALF_UP)
                                           .multiply(BigDecimal.valueOf(100))
                                           .setScale(2, RoundingMode.HALF_UP);
                    return new DashboardSummaryDto.AssetSlice(
                            e.getAsset().getId(),
                            e.getAsset().getName(),
                            e.getAsset().getInstitution(),
                            e.getAsset().getGroup().getId(),
                            e.getAsset().getGroup().getName(),
                            e.getBalance(),
                            pct);
                })
                .sorted(Comparator.comparing(DashboardSummaryDto.AssetSlice::balance).reversed())
                .toList();

        return new DashboardSummaryDto(
                year, month,
                totalBalance, previousBalance, variation, variationPercent,
                history, groupDistribution, assetDistribution
        );
    }
}
