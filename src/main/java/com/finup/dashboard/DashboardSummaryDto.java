package com.finup.dashboard;

import java.math.BigDecimal;
import java.util.List;

public record DashboardSummaryDto(
        // Current period
        short year,
        short month,
        BigDecimal totalBalance,
        BigDecimal previousBalance,
        BigDecimal variation,
        BigDecimal variationPercent,

        // Chart data — total patrimônio por mês
        List<PeriodPoint> history,

        // Donut data — distribuição por grupo no mês atual
        List<GroupSlice> groupDistribution,

        // Table data — detalhamento por ativo no mês atual
        List<AssetSlice> assetDistribution
) {
    public record PeriodPoint(
            short year,
            short month,
            String label,
            BigDecimal total
    ) {}

    public record GroupSlice(
            Long groupId,
            String groupName,
            BigDecimal total,
            BigDecimal percent
    ) {}

    public record AssetSlice(
            Long assetId,
            String assetName,
            String institution,
            Long groupId,
            String groupName,
            BigDecimal balance,
            BigDecimal percent
    ) {}
}
