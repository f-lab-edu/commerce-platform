package com.commerce.platform.core.application.port.in.dto;

import com.commerce.platform.core.domain.enums.StockOperation;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.shared.vo.Quantity;

public record UpdateStockCommand(
        ProductId productId,
        Quantity quantity,
        StockOperation stockOperation
) {
}
