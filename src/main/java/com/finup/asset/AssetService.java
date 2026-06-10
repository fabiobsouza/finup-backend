package com.finup.asset;

import com.finup.assetgroup.AssetGroup;
import com.finup.assetgroup.AssetGroupRepository;
import com.finup.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository      assetRepository;
    private final AssetGroupRepository groupRepository;

    @Transactional(readOnly = true)
    public List<AssetDto> listByUser(User user, boolean activeOnly) {
        Boolean filter = activeOnly ? Boolean.TRUE : null; // null = todos
        return assetRepository
                .findByUserWithGroup(user, filter)
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
        Asset asset = assetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ativo não encontrado"));

        AssetGroup group = groupRepository.findByIdAndUser(req.groupId(), user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Grupo não encontrado"));

        asset.setGroup(group);
        asset.setName(req.name());
        asset.setInstitution(req.institution());
        asset.setDescription(req.description());
        if (req.displayOrder() != null) asset.setDisplayOrder(req.displayOrder());
        return AssetDto.from(assetRepository.save(asset));
    }

    @Transactional
    public void deactivate(User user, Long id) {
        Asset asset = assetRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ativo não encontrado"));
        asset.setActive(false);
        assetRepository.save(asset);
    }
}
