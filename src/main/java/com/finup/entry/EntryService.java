package com.finup.entry;

import com.finup.asset.Asset;
import com.finup.asset.AssetRepository;
import com.finup.common.Periods;
import com.finup.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EntryService {

    private final MonthlyEntryRepository entryRepository;
    private final AssetRepository        assetRepository;

    @Transactional(readOnly = true)
    public List<EntryDto> getByPeriod(User user, short year, short month) {
        return entryRepository.findByUserAndYearAndMonthFetch(user.getId(), (int) year, (int) month)
                .stream().map(EntryDto::from).toList();
    }

    @Transactional
    public List<EntryDto> saveBatch(User user, EntryBatchRequest req) {
        List<EntryDto> results = new ArrayList<>();
        YearMonth period = YearMonth.of(req.year(), req.month());

        for (EntryBatchRequest.EntryItem item : req.entries()) {
            Asset asset = assetRepository.findByIdAndUser(item.assetId(), user)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Ativo não encontrado: " + item.assetId()));

            if (!asset.acceptsEntriesIn(period)) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "O ativo \"" + asset.getName() + "\" foi liquidado em "
                                + Periods.format(YearMonth.from(asset.getLiquidatedAt()))
                                + " e não aceita lançamentos a partir desse mês.");
            }

            MonthlyEntry entry = entryRepository
                    .findByAssetAndYearAndMonth(asset, req.year(), req.month())
                    .orElseGet(() -> MonthlyEntry.builder()
                            .asset(asset)
                            .user(user)
                            .year(req.year())
                            .month(req.month())
                            .build());

            entry.setBalance(item.balance());
            entry.setNotes(item.notes());
            results.add(EntryDto.from(entryRepository.save(entry)));
        }

        return results;
    }
}
