package com.commerce.platform.bootstrap.dto.product;

import com.commerce.platform.core.domain.enums.StockOperation;

public record UpdateStockRequest(
        Long quantity,
        StockOperation operation
) { }