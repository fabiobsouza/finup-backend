package com.finup.report;

import com.finup.asset.Asset;
import com.finup.assetgroup.AssetGroup;
import com.finup.entry.MonthlyEntry;
import com.finup.entry.MonthlyEntryRepository;
import com.finup.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnualReportServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-15T12:00:00Z"), ZoneId.of("UTC"));

    @Mock
    private MonthlyEntryRepository entryRepository;

    private AnnualReportService service;

    private final User user = User.builder().id(1L).build();
    private final AssetGroup reserva = AssetGroup.builder().id(10L).name("Reserva").displayOrder(0).build();
    private final AssetGroup acoes   = AssetGroup.builder().id(11L).name("Ações").displayOrder(1).build();

    @BeforeEach
    void setUp() {
        service = new AnnualReportService(entryRepository, CLOCK);
    }

    private static MonthlyEntry entry(Asset asset, int year, int month, String balance) {
        return MonthlyEntry.builder().asset(asset).year((short) year).month((short) month)
                .balance(new BigDecimal(balance)).build();
    }

    @Test
    void testAnnualMatrix() {
        Asset cdi  = Asset.builder().id(1L).name("CDI Itaú").group(reserva).displayOrder(0).build();
        Asset petr = Asset.builder().id(2L).name("PETR4").group(acoes).displayOrder(0)
                .liquidatedAt(LocalDate.of(2026, 3, 1)).build();

        when(entryRepository.findForAnnualReport(1L, 2026, 2025)).thenReturn(List.of(
                entry(petr, 2026, 1, "500.00"),
                entry(petr, 2026, 2, "600.00"),
                entry(cdi, 2025, 12, "1000.00"),
                entry(cdi, 2026, 1, "1010.00"),
                entry(cdi, 2026, 3, "1100.00")));
        when(entryRepository.findDistinctYears(1L)).thenReturn(List.of((short) 2025, (short) 2026));

        AnnualReportDto report = service.getAnnual(user, null);

        assertEquals(2026, report.year());
        assertEquals(List.of(2025, 2026), report.availableYears());

        // ordenação por categoria (display_order) e depois ativo
        assertEquals(2, report.rows().size());
        AnnualReportDto.AssetRow cdiRow = report.rows().get(0);
        AnnualReportDto.AssetRow petrRow = report.rows().get(1);
        assertEquals("CDI Itaú", cdiRow.assetName());
        assertEquals("PETR4", petrRow.assetName());
        assertEquals(LocalDate.of(2026, 3, 1), petrRow.liquidatedAt());

        // meses sem lançamento ficam nulos
        List<BigDecimal> cdiMonths = cdiRow.series().months();
        assertEquals(12, cdiMonths.size());
        assertEquals(new BigDecimal("1010.00"), cdiMonths.get(0));
        assertNull(cdiMonths.get(1));
        assertEquals(new BigDecimal("1100.00"), cdiMonths.get(2));

        // variação com base em dez/ano anterior
        assertEquals(new BigDecimal("1000.00"), cdiRow.series().previousYearEnd());
        assertEquals(new BigDecimal("1100.00"), cdiRow.series().lastBalance());
        assertEquals(new BigDecimal("100.00"), cdiRow.series().variation());
        assertEquals(new BigDecimal("10.00"), cdiRow.series().variationPercent());

        // sem dez/ano anterior: base = primeiro mês com saldo
        assertNull(petrRow.series().previousYearEnd());
        assertEquals(new BigDecimal("100.00"), petrRow.series().variation());
        assertEquals(new BigDecimal("20.00"), petrRow.series().variationPercent());

        // totais por mês
        List<BigDecimal> totals = report.totals().months();
        assertEquals(new BigDecimal("1510.00"), totals.get(0));
        assertEquals(new BigDecimal("600.00"), totals.get(1));
        assertEquals(new BigDecimal("1100.00"), totals.get(2));
        assertNull(totals.get(3));
        assertEquals(new BigDecimal("1000.00"), report.totals().previousYearEnd());
        assertEquals(new BigDecimal("100.00"), report.totals().variation());
    }

    @Test
    void testEmptyYear() {
        when(entryRepository.findForAnnualReport(1L, 2020, 2019)).thenReturn(List.of());
        when(entryRepository.findDistinctYears(1L)).thenReturn(List.of((short) 2025));

        AnnualReportDto report = service.getAnnual(user, 2020);

        assertEquals(2020, report.year());
        assertTrue(report.rows().isEmpty());
        assertEquals(List.of(2020, 2025, 2026), report.availableYears());
        assertNull(report.totals().lastBalance());
        assertNull(report.totals().variation());
    }

    @Test
    void testZeroBaseHasNoPercent() {
        Asset cdi = Asset.builder().id(1L).name("CDI").group(reserva).displayOrder(0).build();
        when(entryRepository.findForAnnualReport(1L, 2026, 2025)).thenReturn(List.of(
                entry(cdi, 2025, 12, "0.00"),
                entry(cdi, 2026, 6, "250.00")));
        when(entryRepository.findDistinctYears(1L)).thenReturn(List.of((short) 2026));

        AnnualReportDto report = service.getAnnual(user, 2026);

        assertEquals(new BigDecimal("250.00"), report.rows().get(0).series().variation());
        assertNull(report.rows().get(0).series().variationPercent());
    }
}
