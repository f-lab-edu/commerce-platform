package com.commerce.coupon.core.infrastructure.config;

import com.commerce.coupon.core.application.port.in.CouponIssueUseCase;
import com.commerce.coupon.core.infrastructure.event.CouponIssueRequestEvent;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 쿠폰 발급 요청 컨슈머.
 *
 * 에러 처리 정책:
 * - BusinessException(DUPLICATE_ISSUED_COUPON 등): warn 로그 + 종료 (정상 흐름).
 * - 그 외 예외: throw → ErrorHandler 재시도 → DLT.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponConsumer {

    private final CouponIssueUseCase couponIssueUseCase;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "coupon-issue-request", groupId = "coupon-service")
    public void consume(String message) throws JsonProcessingException {
        log.info("쿠폰 발급 요청 메시지 수신 - message: {}", message);

        CouponIssueRequestEvent event = objectMapper.readValue(message, CouponIssueRequestEvent.class);

        try {
            couponIssueUseCase.issueCoupon(
                    CouponId.of(event.couponId()),
                    CustomerId.of(event.customerId())
            );
        } catch (BusinessException e) {
            log.warn("[Coupon] 쿠폰 발급 비즈니스 거부 - couponId: {}, customerId: {}, code: {}, msg: {}",
                    event.couponId(), event.customerId(), e.getCode(), e.getMessage());
        }
    }
}
