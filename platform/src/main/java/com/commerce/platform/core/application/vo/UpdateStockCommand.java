package com.commerce.platform.core.application.vo;

import com.commerce.platform.core.domain.enums.StockOperation;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.platform.core.domain.vo.Quantity;

public record UpdateStockCommand(
        ProductId productId,
        Quantity quantity,
        StockOperation stockOperation
) {
}
