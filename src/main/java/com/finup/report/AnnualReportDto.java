package com.finup.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Matriz anual: uma linha por ativo com lançamento no ano, 12 saldos mensais
 * (null = sem lançamento) e a variação no ano.
 *
 * Variação no ano = último saldo do ano − saldo de dezembro do ano anterior.
 * Sem dezembro anterior, a base é o primeiro mês com saldo no ano.
 */
public record AnnualReportDto(
        int year,
        List<Integer> availableYears,
        List<AssetRow> rows,
        Series totals
) {
    public record AssetRow(
            Long assetId,
            String assetName,
            String institution,
            Long groupId,
            String groupName,
            LocalDate liquidatedAt,
            Series series
    ) {}

    /** 12 saldos mensais (null = sem lançamento) + variação no ano. */
    public record Series(
            List<BigDecimal> months,
            BigDecimal previousYearEnd,
            BigDecimal lastBalance,
            BigDecimal variation,
            BigDecimal variationPercent
    ) {}
}
