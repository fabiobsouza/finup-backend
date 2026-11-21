package com.finup.asset;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Mês/ano a partir do qual o ativo deixa de existir. */
public record LiquidationRequest(
        @NotNull @Min(1900) @Max(9999) Integer year,
        @NotNull @Min(1) @Max(12) Integer month
) {}
