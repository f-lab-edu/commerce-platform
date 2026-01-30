package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.port.in.CouponIssueUseCase;
import com.commerce.platform.core.application.port.in.dto.CouponView;
import com.commerce.platform.core.domain.aggreate.Coupon;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CouponIssueId;
import com.commerce.shared.vo.CustomerId;
import com.commerce.platform.infrastructure.persistence.CouponIssueRepository;
import com.commerce.platform.infrastructure.persistence.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CouponIssueUseCaseImplTest {
    @Autowired
    private CouponIssueUseCase couponIssueUseCase;
    @Autowired
    private CouponIssueRepository couponIssueRepository;

    @Autowired
    private CouponRepository couponRepository;


    @Test
    @DisplayName("동시 발급 요청 처리, 9명만 발금 가능, 11명 실패")
    void issueCoupon_Concurrent() throws Exception {
        final String COUPON_ID_10 = "C20251102004317354977";
        // given
        int threadCount = 20;
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        CountDownLatch latch = new CountDownLatch(threadCount);

        List<CouponIssueId> issueSuccesses = new ArrayList<>();
        AtomicInteger success = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        // when
        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            executorService.submit(() -> {
                CouponIssueId id = new CouponIssueId(CouponId.of(COUPON_ID_10),
                CustomerId.of("test" + (finalI + 1)));
                try {
                    couponIssueUseCase.issueCoupon(
                            CouponId.of(COUPON_ID_10),
                            CustomerId.of("test" + (finalI + 1))
                    );

                    success.incrementAndGet();
                    issueSuccesses.add(id);
                } catch (Exception e) {
                    failed.incrementAndGet();
                    System.out.println("err " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then - 10개만 발급되어야 함
        Long issuedCount = couponIssueRepository.countByCouponId(CouponId.of(COUPON_ID_10));
        Optional<Coupon> coupon = couponRepository.findById(CouponId.of(COUPON_ID_10));

        assertThat(issuedCount)
                .as("발급이력 count 10개")
                .isEqualTo(10L);
        assertThat(coupon.get().getIssuedQuantity().value())
                .as("coupon issuedQuantity = 10")
                .isEqualTo(10);

        assertThat(failed.get())
                .as("test1 은 이미 발급받았으므로 fail = 11")
                .isEqualTo(11);
        assertThat(success.get())
                .as("9명 성공")
                .isEqualTo(9);

        // 롤백
        couponIssueRepository.deleteAllById(issueSuccesses);
        coupon.get().changeIssuedQuantityForTest(1);
        couponRepository.save(coupon.get());
    }

    @Test
    void test() {
        List<CouponView> test1 = couponIssueUseCase.getMyCoupons(CustomerId.of("test1"));
        System.out.println(test1);
    }
}