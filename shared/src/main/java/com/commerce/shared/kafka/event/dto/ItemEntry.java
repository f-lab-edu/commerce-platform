package com.commerce.shared.kafka.event.dto;

import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;

public record ItemEntry(ProductId productId, Quantity quantity) { }
