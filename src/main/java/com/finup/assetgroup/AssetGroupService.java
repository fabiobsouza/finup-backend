package com.finup.assetgroup;

import com.finup.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetGroupService {

    private final AssetGroupRepository repository;
    private final com.finup.asset.AssetRepository assetRepository;

    @Transactional(readOnly = true)
    public List<AssetGroupDto> listByUser(User user) {
        return repository.findByUserOrderByDisplayOrderAscNameAsc(user)
                .stream()
                .map(g -> {
                    int count = assetRepository.findByGroupAndUserOrderByDisplayOrderAscNameAsc(g, user)
                            .stream().filter(a -> Boolean.TRUE.equals(a.getActive())).mapToInt(a -> 1).sum();
                    return AssetGroupDto.from(g, count);
                }).toList();
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
        repository.delete(group);
    }
}
