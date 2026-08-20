package com.commerce.coupon.bootstrap.event;

import com.commerce.coupon.bootstrap.event.dto.CouponApplyEvent;
import com.commerce.coupon.bootstrap.event.dto.CouponRestoreEvent;
import com.commerce.coupon.core.application.port.in.CouponIssueUseCase;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.TransactionalEventPublisher;
import com.commerce.shared.kafka.event.dto.CouponApplyFailedEvent;
import com.commerce.shared.kafka.event.dto.CouponAppliedEvent;
import com.commerce.shared.kafka.event.topic.EventTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 쿠폰 적용/복구 saga 컨슈머.
 * 비즈니스 로직은 CouponIssueUseCase에 위임.
 *
 * 페이로드 수신: coupon 모듈 로컬 DTO({@link CouponApplyEvent}, {@link CouponRestoreEvent})로
 * 직접 수신.
 *
 * 에러 처리 정책:
 * - BusinessException(쿠폰 만료/미발급/타 주문 사용 등): 보상 이벤트(CouponApplyFailedEvent) 발행 + 종료.
 * - 그 외 예외: throw → ErrorHandler 재시도 → DLT.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderCouponConsumer {

    private final CouponIssueUseCase couponIssueUseCase;
    private final TransactionalEventPublisher transactionalEventPublisher;

    @KafkaListener(topics = "order.priced", groupId = "coupon-service")
    public void handleApplyCoupon(CouponApplyEvent event) {
        log.info("[Coupon] order.priced 수신 - orderId: {}, couponId: {}", event.orderId(), event.couponId());

        long discountAmt;
        try {
            discountAmt = couponIssueUseCase.applyCouponForSaga(
                    event.couponId(), event.customerId(), event.orderId(), event.originAmt()
            );
        } catch (BusinessException e) {
            log.warn("[Coupon] 쿠폰 적용 비즈니스 실패 - orderId: {}, code: {}, msg: {}",
                    event.orderId(), e.getCode(), e.getMessage());
            transactionalEventPublisher.publish(EventTopic.COUPON_APPLY_FAILED_TOPIC,
                    new CouponApplyFailedEvent(
                            event.orderId(), event.items(), e.getMessage(),
                            event.orderId(), LocalDateTime.now()
                    )
            );
            return;
        }

        transactionalEventPublisher.publish(EventTopic.COUPON_APPLIED_TOPIC,
                new CouponAppliedEvent(
                        event.orderId(), discountAmt, event.originAmt(),
                        event.items(), event.customerId(), event.couponId(),
                        event.payMethod(), event.payProvider(),
                        event.orderId(), LocalDateTime.now()
                )
        );
    }

    @KafkaListener(topics = {"payment.failed"}, groupId = "coupon-service")
    public void handleRestoreCoupon(CouponRestoreEvent event) {
        log.info("[Coupon] 보상 이벤트 수신 - orderId: {}, couponId: {}", event.orderId(), event.couponId());
        couponIssueUseCase.restoreCouponForSaga(event.couponId(), event.customerId(), event.orderId());
    }
}
