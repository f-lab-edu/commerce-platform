package com.commerce.platform.bootstrap.dto.product;

import com.commerce.platform.core.domain.enums.StockOperation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateStockRequest(
        @Min(value = 0, message = "음수는 불가합니다.")
        long quantity,
        @NotNull
        StockOperation operation
) { }