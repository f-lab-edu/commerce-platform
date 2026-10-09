package com.commerce.product.bootstrap.event;

import com.commerce.product.bootstrap.event.dto.ProductPricingEvent;
import com.commerce.product.core.application.port.out.ProductOutputPort;
import com.commerce.product.core.domain.aggregate.Product;
import com.commerce.shared.exception.BusinessError;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.ItemEntry;
import com.commerce.shared.kafka.event.dto.OrderPriceFailedEvent;
import com.commerce.shared.kafka.event.dto.OrderPricedEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import com.commerce.shared.vo.ProductId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 금액 계산 saga 컨슈머.
 * inventory.deducted 수신 -> 상품 단가 x 수량 = originAmt 계산 -> OrderPricedEvent 발행
 * Product는 Order DB에 접근하지 않는다. 금액은 이벤트 페이로드로만 전달.
 *
 * 페이로드 수신: product 모듈 로컬 DTO({@link ProductPricingEvent}).
 *
 * 에러 처리 정책:
 * - BusinessException(PRODUCT_NOT_FOUND 등): 보상 이벤트(OrderPriceFailedEvent) 발행 + 종료.
 * - 그 외 예외: throw → ErrorHandler 재시도 → DLT.
 * - 멱등성: 추후 outbox로 일괄 도입 예정.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderProductConsumer {

    private final ProductOutputPort productOutputPort;
    private final TransactionalEventPublisher transactionalEventPublisher;

    @Transactional
    @KafkaListener(topics = "inventory.deducted", groupId = "product-service")
    public void handleCalculatePricing(ProductPricingEvent event) {
        log.info("[Product] inventory.deducted 수신 - orderId: {}", event.orderId());

        long originAmt;
        try {
            originAmt = calculateOriginAmt(event);
        } catch (BusinessException e) {
            log.warn("[Product] 금액 계산 비즈니스 실패 - orderId: {}, code: {}, msg: {}",
                    event.orderId(), e.getCode(), e.getMessage());
            transactionalEventPublisher.publish(EventTopic.ORDER_PRICE_FAILED_TOPIC,
                    new OrderPriceFailedEvent(
                            event.orderId(), event.customerId(), event.couponId(),
                            event.items(), event.payMethod(), event.payProvider(), event.installment(),
                            e.getMessage(),
                            event.orderId(), LocalDateTime.now())
            );
            return;
        }

        transactionalEventPublisher.publish(EventTopic.ORDER_PRICED_TOPIC,
                new OrderPricedEvent(
                        event.orderId(), event.customerId(), event.couponId(),
                        event.items(), event.payMethod(), event.payProvider(), event.installment(),
                        originAmt,
                        event.orderId(), LocalDateTime.now()
                )
        );
        log.info("[Product] 금액 계산 완료 - orderId: {}, originAmt: {}", event.orderId(), originAmt);
    }

    private long calculateOriginAmt(ProductPricingEvent event) {
        long total = 0;
        for (ItemEntry item : event.items()) {
            Product product = productOutputPort.findById(item.productId())
                    .orElseThrow(() -> new BusinessException(BusinessError.PRODUCT_NOT_FOUND));
            total += product.getPrice().value() * item.quantity().value();
        }
        return total;
    }
}
