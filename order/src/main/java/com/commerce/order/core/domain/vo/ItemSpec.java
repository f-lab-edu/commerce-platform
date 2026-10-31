package com.commerce.order.core.domain.vo;

import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;

/**
 * 주문상품 명세(상품 + 수량). 이벤트 계약(ItemEntry)과의 변환은 bootstrap/infrastructure에서만 한다.
 */
public record ItemSpec(ProductId productId, Quantity quantity) { }
