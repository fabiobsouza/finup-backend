package com.finup.entry;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EntryDto(
        Long id,
        Long assetId,
        String assetName,
        Long groupId,
        String groupName,
        short year,
        short month,
        BigDecimal balance,
        String notes,
        LocalDateTime updatedAt
) {
    public static EntryDto from(MonthlyEntry e) {
        return new EntryDto(
                e.getId(),
                e.getAsset().getId(),
                e.getAsset().getName(),
                e.getAsset().getGroup().getId(),
                e.getAsset().getGroup().getName(),
                e.getYear(),
                e.getMonth(),
                e.getBalance(),
                e.getNotes(),
                e.getUpdatedAt()
        );
    }
}
