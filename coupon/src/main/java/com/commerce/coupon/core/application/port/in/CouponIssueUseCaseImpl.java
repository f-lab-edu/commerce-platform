package com.commerce.coupon.core.application.port.in;


import com.commerce.coupon.core.application.port.in.dto.CouponView;
import com.commerce.coupon.core.application.port.out.CouponIssueCache;
import com.commerce.coupon.core.application.port.out.CouponIssueOutPort;
import com.commerce.coupon.core.application.port.out.CouponOutPort;
import com.commerce.coupon.core.domain.aggregate.Coupon;
import com.commerce.coupon.core.domain.aggregate.CouponIssues;
import com.commerce.coupon.core.infrastructure.event.CouponIssueRequestEvent;
import com.commerce.shared.exception.BusinessException;
import com.commerce.shared.kafka.KafkaEventPublisher;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CouponIssueId;
import com.commerce.shared.vo.CustomerId;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.commerce.shared.exception.BusinessError.DUPLICATE_ISSUED_COUPON;
import static com.commerce.shared.exception.BusinessError.INVALID_COUPON;
import static com.commerce.shared.kafka.event.topic.EventTopic.COUPON_ISSUE_TOPIC;

@Transactional
@Log4j2
@RequiredArgsConstructor
@Service
public class CouponIssueUseCaseImpl implements CouponIssueUseCase{
    private final CouponOutPort couponOutPort;
    private final CouponIssueOutPort couponIssueOutPort;
    private final KafkaEventPublisher eventPublisher;
    private final CouponIssueCache couponIssueCache;

    @Override
    @Transactional(readOnly = true)
    public List<CouponView> getMyCoupons(CustomerId customerId) {
        List<CouponIssues> myCoupons = couponIssueOutPort.findByCustomerId(customerId);

        if(myCoupons.isEmpty()) return List.of();

        List<CouponId> couponIds = myCoupons.stream()
                .map(ci -> ci.getCouponIssueId().couponId())
                .toList();

        Map<CouponId, Coupon> couponMap = couponOutPort.findByIdIn(couponIds)
                .stream()
                .collect(Collectors.toMap(Coupon::getCouponId, Function.identity()));

        return myCoupons.stream()
                .map(item -> {
                    Coupon coupon = couponMap.get(item.getCouponIssueId().couponId());
                    return new CouponView(coupon.getCouponName(),
                            coupon.getDiscountPercent(),
                            coupon.getMinOrderAmt().value(),
                            coupon.getMaxDiscountAmt().value(),
                            item.getStatus().getValue(),
                            LocalDate.now().until(coupon.getValidPeriod().toDt()).getDays());
                })
                .toList();
    }

    @Override
    public void issueCoupon(CouponId couponId, CustomerId customerId) {
        // 캐싱데이터 조회
        if(isIssued(couponId, customerId)) return;

        // db에서 발행여부 조회
        CouponIssueId couponIssueId = new CouponIssueId(couponId, customerId);
        couponIssueOutPort.findByCouponIssueId(couponIssueId)
                .ifPresent(exist -> {
                    throw new BusinessException(DUPLICATE_ISSUED_COUPON);
                });

        // 발행
        Coupon coupon = couponOutPort.findById(couponId)
                .orElseThrow(() -> new BusinessException(INVALID_COUPON));
        log.info("coupon issuedQuantity : " + coupon.getIssuedQuantity().value());

        coupon.issueCoupon();
        couponIssueOutPort.save(CouponIssues.create(couponId, customerId));

        // 발급완료된 고객 저장
        couponIssueCache.save(couponId, customerId);
    }

    @Override
    public void requestIssueCoupon(CouponId couponId, CustomerId customerId) {
        CouponIssueRequestEvent event = CouponIssueRequestEvent.of(couponId, customerId);
        eventPublisher.publish(COUPON_ISSUE_TOPIC, event);
    }

    @Override
    public boolean isIssued(CouponId couponId, CustomerId customerId) {
        return couponIssueCache.isIssued(couponId, customerId);
    }
}
