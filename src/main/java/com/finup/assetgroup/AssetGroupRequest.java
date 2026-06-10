package com.finup.assetgroup;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssetGroupRequest(
        @NotBlank @Size(min = 1, max = 100) String name,
        Integer displayOrder
) {}
