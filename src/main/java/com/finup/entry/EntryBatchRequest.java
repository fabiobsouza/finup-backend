package com.finup.entry;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record EntryBatchRequest(
        @NotNull short year,
        @Min(1) @Max(12) short month,
        @NotNull @Size(min = 1) List<EntryItem> entries
) {
    public record EntryItem(
            @NotNull Long assetId,
            @NotNull @DecimalMin("0.00") BigDecimal balance,
            @Size(max = 500) String notes
    ) {}
}
