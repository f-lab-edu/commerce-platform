package com.commerce.inventory.bootstrap.dto;

import com.commerce.inventory.core.domain.enums.StockOperation;
import com.commerce.shared.vo.Quantity;
import jakarta.validation.constraints.NotNull;

public record UpdateStockRequest(
        Quantity quantity,
        @NotNull
        StockOperation operation
) { }
