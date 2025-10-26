package com.commerce.platform.infrastructure.persistence;

import com.commerce.platform.core.application.out.OrderReadOutPort;
import com.commerce.platform.core.domain.aggreate.OrderItem;
import com.commerce.platform.core.domain.dto.ProductView;
import com.commerce.platform.core.domain.vo.ProductId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class OrderReadAdaptor implements OrderReadOutPort {
    @Override
    public Map<ProductId, ProductView> getOrderItemsByProductId(List<OrderItem> orderItems) {
        return null;
    }
}
