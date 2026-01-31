package com.commerce.coupon.core.application.port.in;


import com.commerce.coupon.core.application.port.in.dto.CouponView;
import com.commerce.coupon.core.application.port.out.CouponIssueOutPort;
import com.commerce.coupon.core.application.port.out.CouponOutPort;
import com.commerce.coupon.core.domain.aggregate.Coupon;
import com.commerce.coupon.core.domain.aggregate.CouponIssues;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CouponIssueId;
import com.commerce.shared.vo.CustomerId;
import com.commerce.shared.exception.BusinessException;
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

@Log4j2
@RequiredArgsConstructor
@Service
public class CouponIssueUseCaseImpl implements CouponIssueUseCase{
    private final CouponOutPort couponOutPort;
    private final CouponIssueOutPort couponIssueOutPort;
//    private final LockFacade lockFacade;

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
        CouponIssueId couponIssueId = new CouponIssueId(couponId, customerId);
        // 이미 다운받았는지 확인
        couponIssueOutPort.findByCouponIssueId(couponIssueId)
                .ifPresent(exist -> {
                    throw new BusinessException(DUPLICATE_ISSUED_COUPON);
                });

        // 발행
        // 여기 내부에서 새로운 트랜잭션으로 실행된다.
        // todo
        /*lockFacade.executeWithLock("COUPON_ISSUE_", couponId.id(), () -> {
            Coupon coupon = couponOutPort.findById(couponId)
                    .orElseThrow(() -> new BusinessException(INVALID_COUPON));
            log.info("coupon issuedQuantity : " + coupon.getIssuedQuantity().value());
            // 쿠폰 발급
            coupon.issueCoupon();
            // 발급 이력 저장
            couponIssueOutPort.save(CouponIssues.create(couponId, customerId));
            return null;
        });*/

    }
}
