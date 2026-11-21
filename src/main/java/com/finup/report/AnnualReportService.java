package com.finup.report;

import com.finup.asset.Asset;
import com.finup.entry.MonthlyEntry;
import com.finup.entry.MonthlyEntryRepository;
import com.finup.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Year;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnnualReportService {

    private static final Comparator<Asset> ROW_ORDER = Comparator
            .comparing((Asset a) -> a.getGroup().getDisplayOrder())
            .thenComparing(a -> a.getGroup().getName(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Asset::getDisplayOrder)
            .thenComparing(Asset::getName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Asset::getId);

    private final MonthlyEntryRepository entryRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public AnnualReportDto getAnnual(User user, Integer requestedYear) {
        int currentYear = Year.now(clock).getValue();
        int year = requestedYear != null ? requestedYear : currentYear;

        List<MonthlyEntry> entries = entryRepository.findForAnnualReport(user.getId(), year, year - 1);

        Map<Long, Asset> assets = new HashMap<>();
        Map<Long, BigDecimal[]> monthsByAsset = new HashMap<>();
        Map<Long, BigDecimal> previousYearEndByAsset = new HashMap<>();
        BigDecimal[] monthTotals = new BigDecimal[12];
        BigDecimal previousYearEndTotal = null;

        for (MonthlyEntry e : entries) {
            Asset asset = e.getAsset();
            if (e.getYear() != year) { // dezembro do ano anterior
                previousYearEndByAsset.put(asset.getId(), e.getBalance());
                previousYearEndTotal = add(previousYearEndTotal, e.getBalance());
                continue;
            }
            int idx = e.getMonth() - 1;
            assets.putIfAbsent(asset.getId(), asset);
            monthsByAsset.computeIfAbsent(asset.getId(), k -> new BigDecimal[12])[idx] = e.getBalance();
            monthTotals[idx] = add(monthTotals[idx], e.getBalance());
        }

        List<AnnualReportDto.AssetRow> rows = assets.values().stream()
                .sorted(ROW_ORDER)
                .map(a -> new AnnualReportDto.AssetRow(
                        a.getId(),
                        a.getName(),
                        a.getInstitution(),
                        a.getGroup().getId(),
                        a.getGroup().getName(),
                        a.getLiquidatedAt(),
                        summarize(previousYearEndByAsset.get(a.getId()), monthsByAsset.get(a.getId()))))
                .toList();

        return new AnnualReportDto(
                year,
                availableYears(user, year, currentYear),
                rows,
                summarize(previousYearEndTotal, monthTotals));
    }

    private List<Integer> availableYears(User user, int year, int currentYear) {
        SortedSet<Integer> years = new TreeSet<>();
        entryRepository.findDistinctYears(user.getId()).forEach(y -> years.add(y.intValue()));
        years.add(currentYear);
        years.add(year);
        return List.copyOf(years);
    }

    static AnnualReportDto.Series summarize(BigDecimal previousYearEnd, BigDecimal[] months) {
        BigDecimal first = null;
        BigDecimal last = null;
        for (BigDecimal v : months) {
            if (v == null) continue;
            if (first == null) first = v;
            last = v;
        }

        List<BigDecimal> monthList = Arrays.asList(months);
        if (last == null) {
            return new AnnualReportDto.Series(monthList, previousYearEnd, null, null, null);
        }

        BigDecimal base = previousYearEnd != null ? previousYearEnd : first;
        BigDecimal variation = last.subtract(base);
        BigDecimal percent = base.signum() == 0 ? null
                : variation.divide(base, 4, RoundingMode.HALF_UP)
                           .multiply(BigDecimal.valueOf(100))
                           .setScale(2, RoundingMode.HALF_UP);
        return new AnnualReportDto.Series(monthList, previousYearEnd, last, variation, percent);
    }

    private static BigDecimal add(BigDecimal acc, BigDecimal value) {
        return acc == null ? value : acc.add(value);
    }
}
