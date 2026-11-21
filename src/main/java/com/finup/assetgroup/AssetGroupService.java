package com.finup.assetgroup;

import com.finup.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AssetGroupService {

    private final AssetGroupRepository repository;
    private final com.finup.asset.AssetRepository assetRepository;

    @Transactional(readOnly = true)
    public List<AssetGroupDto> listByUser(User user) {
        // Contagem de ativos não liquidados por grupo, em uma única consulta
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : assetRepository.countNotLiquidatedByGroup(user)) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return repository.findByUserOrderByDisplayOrderAscNameAsc(user)
                .stream()
                .map(g -> AssetGroupDto.from(g, counts.getOrDefault(g.getId(), 0L).intValue()))
                .toList();
    }

    @Transactional
    public AssetGroupDto create(User user, AssetGroupRequest req) {
        AssetGroup group = AssetGroup.builder()
                .user(user)
                .name(req.name())
                .displayOrder(req.displayOrder() != null ? req.displayOrder() : 0)
                .build();
        return AssetGroupDto.from(repository.save(group));
    }

    @Transactional
    public AssetGroupDto update(User user, Long id, AssetGroupRequest req) {
        AssetGroup group = repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo não encontrado"));
        group.setName(req.name());
        if (req.displayOrder() != null) group.setDisplayOrder(req.displayOrder());
        return AssetGroupDto.from(repository.save(group));
    }

    @Transactional
    public void delete(User user, Long id) {
        AssetGroup group = repository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grupo não encontrado"));
        long assets = assetRepository.countByGroup(group);
        if (assets > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A categoria possui " + assets + (assets == 1 ? " ativo" : " ativos")
                            + " (incluindo liquidados). Mova-os para outra categoria antes de excluir.");
        }
        repository.delete(group);
    }
}
