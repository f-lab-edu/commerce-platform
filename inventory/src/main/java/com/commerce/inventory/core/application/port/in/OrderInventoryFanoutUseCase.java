package com.commerce.inventory.core.application.port.in;

import com.commerce.shared.kafka.event.dto.ItemEntry;

import java.util.List;

/**
 * B2 fan-out 인바운드 포트. 주문을 차감하지 않고 상품 단위 커맨드로 분해(scatter)만 한다.
 *
 * 벤치마크는 order.created를 거치지 않고 이 메서드를 직접 호출한다.
 */
public interface OrderInventoryFanoutUseCase {

    /**
     * 각 상품마다 inventory.stock-command(DEDUCT, key=productId)를 발행한다. 주문 레벨 컨텍스트
     * (customerId/couponId/payMethod/payProvider)는 별도 저장 없이 각 이벤트 페이로드로 운반한다.
     * fan-out 멱등(중복 발행 방지) 등 컨슈머 멱등은 추후 일괄 적용한다(스펙 §12).
     */
    void fanout(String orderId, String customerId, String couponId,
                List<ItemEntry> items, String payMethod, String payProvider);
}
