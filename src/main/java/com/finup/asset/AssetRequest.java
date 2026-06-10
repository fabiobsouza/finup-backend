package com.finup.asset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AssetRequest(
        @NotNull Long groupId,
        @NotBlank @Size(min = 1, max = 150) String name,
        @Size(max = 150) String institution,
        @Size(max = 300) String description,
        Integer displayOrder
) {}
