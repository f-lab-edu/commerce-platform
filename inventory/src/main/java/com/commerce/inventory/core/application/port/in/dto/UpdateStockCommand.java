package com.commerce.inventory.core.application.port.in.dto;

import com.commerce.inventory.core.domain.enums.StockOperation;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;

public record UpdateStockCommand(
        ProductId productId,
        Quantity quantity,
        StockOperation stockOperation
) {
}