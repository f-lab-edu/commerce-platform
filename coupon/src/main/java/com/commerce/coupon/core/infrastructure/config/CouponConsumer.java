package com.commerce.coupon.core.infrastructure.config;

import com.commerce.coupon.core.application.port.in.CouponIssueUseCase;
import com.commerce.coupon.core.infrastructure.event.CouponIssueRequestEvent;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponConsumer {
    
    private final CouponIssueUseCase couponIssueUseCase;
    private final ObjectMapper objectMapper;
    
    @KafkaListener(topics = "coupon-issue-request", groupId = "coupon-service")
    public void consume(String message, Acknowledgment ack) {
        try {
            log.info("쿠폰 발급 요청 메시지 수신 - message: {}", message);
            
            CouponIssueRequestEvent event = objectMapper.readValue(message, CouponIssueRequestEvent.class);
            
            couponIssueUseCase.issueCoupon(
                    CouponId.of(event.couponId()),
                    CustomerId.of(event.customerId())
            );
            
            ack.acknowledge();
        } catch (Exception e) {
            log.error("쿠폰 발급 처리 실패", e);
        }
    }
}