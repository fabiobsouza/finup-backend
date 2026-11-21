package com.finup.asset;

import com.finup.assetgroup.AssetGroup;
import com.finup.assetgroup.AssetGroupRepository;
import com.finup.common.Periods;
import com.finup.entry.MonthlyEntryRepository;
import com.finup.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository        assetRepository;
    private final AssetGroupRepository   groupRepository;
    private final MonthlyEntryRepository entryRepository;
    private final Clock                  clock;

    @Transactional(readOnly = true)
    public List<AssetDto> listByUser(User user, boolean includeLiquidated) {
        return assetRepository
                .findByUserWithGroup(user, includeLiquidated)
                .stream().map(AssetDto::from).toList();
    }

    @Transactional
    public AssetDto create(User user, AssetRequest req) {
        AssetGroup group = groupRepository.findByIdAndUser(req.groupId(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grupo não encontrado"));

        Asset asset = Asset.builder()
                .user(user)
                .group(group)
                .name(req.name())
                .institution(req.institution())
                .description(req.description())
                .displayOrder(req.displayOrder() != null ? req.displayOrder() : 0)
                .build();
        return AssetDto.from(assetRepository.save(asset));
    }

    @Transactional
    public AssetDto update(User user, Long id, AssetRequest req) {
        Asset asset = findOwned(user, id);

        AssetGroup group = groupRepository.findByIdAndUser(req.groupId(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grupo não encontrado"));

        asset.setGroup(group);
        asset.setName(req.name());
        asset.setInstitution(req.institution());
        asset.setDescription(req.description());
        if (req.displayOrder() != null) asset.setDisplayOrder(req.displayOrder());
        return AssetDto.from(assetRepository.save(asset));
    }

    /**
     * Liquida o ativo a partir do mês informado: ele continua aparecendo nos
     * meses anteriores e deixa de aceitar lançamentos desse mês em diante.
     * Recusa se já houver lançamento no mês informado ou depois.
     */
    @Transactional
    public AssetDto liquidate(User user, Long id, LiquidationRequest req) {
        Asset asset = findOwned(user, id);
        YearMonth period = YearMonth.of(req.year(), req.month());

        // Permite até o mês seguinte ao atual: é o primeiro mês possível
        // quando o mês corrente já tem lançamento.
        YearMonth limit = YearMonth.now(clock).plusMonths(1);
        if (period.isAfter(limit)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O mês de liquidação não pode ser posterior a " + Periods.format(limit) + ".");
        }

        entryRepository.findFirstByAssetOrderByYearDescMonthDesc(asset).ifPresent(last -> {
            YearMonth lastPeriod = YearMonth.of(last.getYear(), last.getMonth());
            if (!lastPeriod.isBefore(period)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "O ativo tem lançamento em " + Periods.format(lastPeriod)
                                + ". Liquide a partir de " + Periods.format(lastPeriod.plusMonths(1)) + ".");
            }
        });

        asset.setLiquidatedAt(period.atDay(1));
        return AssetDto.from(assetRepository.save(asset));
    }

    @Transactional
    public AssetDto undoLiquidation(User user, Long id) {
        Asset asset = findOwned(user, id);
        asset.setLiquidatedAt(null);
        return AssetDto.from(assetRepository.save(asset));
    }

    /** Exclusão física, apenas para cadastros sem nenhum lançamento. */
    @Transactional
    public void delete(User user, Long id) {
        Asset asset = findOwned(user, id);
        if (entryRepository.existsByAsset(asset)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O ativo possui lançamentos e não pode ser excluído. Use Liquidar para encerrá-lo.");
        }
        assetRepository.delete(asset);
    }

    private Asset findOwned(User user, Long id) {
        return assetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ativo não encontrado"));
    }
}
