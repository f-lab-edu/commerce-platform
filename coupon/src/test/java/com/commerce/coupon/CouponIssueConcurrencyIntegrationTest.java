package com.commerce.coupon;

import com.commerce.coupon.core.application.port.in.CouponIssueUseCase;
import com.commerce.shared.vo.CouponId;
import com.commerce.shared.vo.CustomerId;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 쿠폰 발급 동시성 통합 테스트
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CouponIssueConcurrencyIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private CouponIssueUseCase couponIssueUseCase;
    
    private String baseUrl;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        log.info("테스트 시작 - baseUrl: {}", baseUrl);
    }
    
    @Test
    @DisplayName("동일한 쿠폰을 11명이 동시에 요청하면, 10명만 성공해야된다.")
    void concurrentCouponIssueTest() throws InterruptedException {
        // given
        String couponId = "C20250201000000000002";
        int threadCount = 11;
        
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // when 10명의 유저가 동시에 쿠폰 발급 요청
        for (int i = 1; i <= threadCount; i++) {
            String customerId = "test" + i;
            
            executorService.submit(() -> {
                try {
                    String url = baseUrl + "/coupons/" + couponId + "/issue/" + customerId;
                    ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
                    
                    log.info("쿠폰 발급 요청 응답 - customerId: {}, status: {}, body: {}", 
                            customerId, response.getStatusCode(), response.getBody());
                    
                    if (response.getStatusCode().is2xxSuccessful()) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    log.error("쿠폰 발급 요청 실패 - customerId: {}", customerId, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        boolean completed = latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // Kafka Consumer가 이벤트를 처리할 시간 대기
        log.info("Kafka Consumer 처리 대기 중...");
        Thread.sleep(5000);
        
        // then Redis 캐싱데이터 개수 = 쿠폰개수 = 10
        int issuedCount = 0;
        for (int i = 1; i <= threadCount; i++) {
            String customerId = "test" + i;
            boolean issued = couponIssueUseCase.checkCouponIssueStatus(
                    CouponId.of(couponId), 
                    CustomerId.of(customerId)
            );
            
            if (issued) {
                issuedCount++;
            } else {
                log.warn("쿠폰 미발급 customerId: {}", customerId);
            }
        }

        assertThat(issuedCount).as("쿠폰제한 10개. redis에 해당쿠폰 발급완료자 수 10").isEqualTo(10);
    }
    
    @Test
    @DisplayName("동일한 유저가 같은 쿠폰을 10번 요청하면, 1번만 처리된다. 발급완료되면 redis에 캐싱한다.")
    void duplicatePreventionTest() throws InterruptedException {
        // given
        String couponId = "C20250201000000000002";
        String customerId = "test1";
        int requestCount = 10;
        
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        CountDownLatch latch = new CountDownLatch(requestCount);
        
        // when 동일한 유저가 10번 요청
        for (int i = 0; i < requestCount; i++) {
            final int attemptNumber = i + 1;
            executorService.submit(() -> {
                try {
                    String url = baseUrl + "/coupons/" + couponId + "/issue/" + customerId;
                    ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
                    log.info("{}번째 요청 완료 - status: {}", attemptNumber, response.getStatusCode());
                } catch (Exception e) {
                    log.error("{}번째 요청 실패", attemptNumber, e);
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();
        
        // Kafka Consumer 처리 대기
        log.info("Kafka Consumer 처리 대기 중...");
        Thread.sleep(5000);
        
        // then: Redis에 1번만 저장됨
        boolean issued = couponIssueUseCase.checkCouponIssueStatus(
                CouponId.of(couponId), 
                CustomerId.of(customerId)
        );
        
        assertThat(issued).isTrue();
    }
    
    @Test
    @DisplayName("쿠폰 발급 확인 API가 정상 동작한다")
    void checkIssuedApiTest() {
        String couponId = "C20250201000000000002";
        String issuedUserId = "test1";

        String url = baseUrl + "/coupons/" + couponId + "/issued/" + issuedUserId;
        ResponseEntity<String> response1 = restTemplate.getForEntity(url, String.class);
        
        assertThat(response1.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response1.getBody()).isEqualTo("발행완료");
    }
}
