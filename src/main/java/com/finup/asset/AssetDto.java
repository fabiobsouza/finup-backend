package com.finup.asset;

import java.time.LocalDate;

public record AssetDto(
        Long id,
        Long groupId,
        String groupName,
        String name,
        String institution,
        String description,
        LocalDate liquidatedAt,
        Integer displayOrder
) {
    public static AssetDto from(Asset a) {
        return new AssetDto(
                a.getId(),
                a.getGroup().getId(),
                a.getGroup().getName(),
                a.getName(),
                a.getInstitution(),
                a.getDescription(),
                a.getLiquidatedAt(),
                a.getDisplayOrder()
        );
    }
}
