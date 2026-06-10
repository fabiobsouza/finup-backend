package com.finup.assetgroup;

import java.time.LocalDateTime;

public record AssetGroupDto(
        Long id,
        String name,
        Integer displayOrder,
        int assetCount,
        LocalDateTime createdAt
) {
    public static AssetGroupDto from(AssetGroup g) {
        return new AssetGroupDto(g.getId(), g.getName(), g.getDisplayOrder(), 0, g.getCreatedAt());
    }

    public static AssetGroupDto from(AssetGroup g, int count) {
        return new AssetGroupDto(g.getId(), g.getName(), g.getDisplayOrder(), count, g.getCreatedAt());
    }
}
