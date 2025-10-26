package com.commerce.platform.core.application.out;

import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.platform.core.domain.dto.ProductView;
import com.commerce.platform.core.domain.vo.ProductId;

import java.util.List;
import java.util.Map;

public interface OrderReadOutPort {
    Map<ProductId, ProductView> getOrderItemsByProductId(List<OrderItem> orderItems);
}
