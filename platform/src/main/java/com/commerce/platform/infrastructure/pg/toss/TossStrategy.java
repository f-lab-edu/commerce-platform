package com.commerce.platform.infrastructure.pg.toss;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.application.out.dto.PgPayCancelResponse;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PgProvider;
import com.commerce.platform.infrastructure.pg.toss.dto.TossCancelResponse;
import com.commerce.platform.infrastructure.pg.toss.dto.TossTransResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static com.commerce.platform.core.domain.enums.PaymentStatus.PARTIAL_CANCELED;

/**
 * TOSS PG
 * 카드, 간편결제, 가상계좌 에 대해 동일한 승인/취소 API 사용
 *
 * 여기서 필요에 따라 토스의 결제수단별 프로세스를 추상화한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public abstract class TossStrategy extends PgStrategy {

    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";
    private static final String TOSS_CANCEL_URL = "https://api.tosspayments.com/v1/payments/";
    private final RestTemplate restTemplate;
    private final String secretKey = "tmp";

    /**
     * TOSS 결제 승인 처리
     * 카드, 간편결제, 가상계좌 모두 동일한 API 사용
     * @param command  승인요청dto
     * @return toss -> 공통dto 변환
     */
    @Override
    public PgPayResponse processApproval(PayOrderCommand command) {
        TossTransResponse response = callTossConfirmApi(
                command.getJsonSubData(),
                command.getOrderId().id(),
                command.getApprovedAmount().value()
        );

        return convertToResponse(response);
    }

    /**
     * TOSS 결제 취소 처리
     * 카드, 간편결제, 가상계좌 모두 동일한 취소 API 사용
     * @param command  취소요청dto
     * @return toss -> 공통dto 변환
     */
    @Override
    public PgPayCancelResponse processCancel(PayCancelCommand command) {
        TossCancelResponse tossCancelResponse = callTossCancelApi(command);

        boolean isSuccess = "DONE".equals(tossCancelResponse.cancelStatus());

        return new PgPayCancelResponse(
                tossCancelResponse.transactionKey(),
                tossCancelResponse.cancelStatus(),
                tossCancelResponse.cancelStatus(),
                tossCancelResponse.cancelReason(),
                tossCancelResponse.cancelAmount(),
                isSuccess
        );
    }

    @Override
    public PgProvider getPgProvider() {
        return PgProvider.TOSS;
    }

    /**
     * TOSS 구현체 중 특정 결제서비스 빈 추출을 위함
     * @return
     */
    @Override
    public PayMethod getPgPayMethod() {
        return getTossPayMethod();
    }

    @Override
    public Object initPayment() {
        return null;
    }

    /**
     * 승인 API 호출
     */
    private TossTransResponse callTossConfirmApi(
            String paymentKey,
            String orderId,
            Long amount
    ) {
        try {
            HttpHeaders headers = createHeaders();
            // 요청 바디
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("paymentKey", paymentKey);
            requestBody.put("orderId", orderId);
            requestBody.put("amount", amount);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            // api 호출
            return restTemplate.exchange(
                    TOSS_CONFIRM_URL,
                    HttpMethod.POST,
                    request,
                    TossTransResponse.class
            ).getBody();

        } catch (HttpClientErrorException e) {
            log.error("TOSS 결제 승인 실패 (4xx) - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(e.getResponseBodyAsString());

        } catch (HttpServerErrorException e) {
            log.error("TOSS 서버 오류 (5xx) - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException(e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("TOSS 승인 API 호출 중 예외 발생", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * TOSS 결제 취소 API 호출 (통합)
     */
    private TossCancelResponse callTossCancelApi(PayCancelCommand command) {
        try {
            String paymentKey = command.getPgTid();                                                  // 결제 키
            Long cancelAmount = command.getCanceledAmount().value();                                 // 취소 금액 (null이면 전액 취소)
            String cancelReason = command.getCancelReason();

            String url = TOSS_CANCEL_URL + paymentKey + "/cancel";

            HttpHeaders headers = createHeaders();

            // 취소 요청 데이터 공통부
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("cancelReason", cancelReason);

            // 부분 취소인 경우 금액 추가
            if (command.getPaymentStatus().equals(PARTIAL_CANCELED)) {
                requestBody.put("cancelAmount", cancelAmount);
            }

            // 결제유혈병 요청 데이터 추가 세팅
            generateCancelRequest(requestBody, command);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<TossTransResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    TossTransResponse.class
            );

            TossTransResponse.CancelInfo lastCancel = response.getBody()
                    .cancels()
                    .get(response.getBody().cancels().size() - 1);

            return new TossCancelResponse(
                    lastCancel.transactionKey(),
                    lastCancel.cancelStatus(),
                    lastCancel.cancelAmount(),
                    lastCancel.cancelReason()
            );

        } catch (HttpClientErrorException e) {
            log.error("토스 결제 취소 실패 (4xx) - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("결제 취소 실패: " + e.getResponseBodyAsString());

        } catch (HttpServerErrorException e) {
            log.error("토스페이먼츠 서버 오류 (5xx) - status: {}, body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("토스페이먼츠 서버 오류: " + e.getResponseBodyAsString());

        } catch (Exception e) {
            log.error("결제 취소 API 호출 중 예외 발생", e);
            throw new RuntimeException("결제 취소 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodeSecretKey());

        return headers;
    }

    /**
     * Secret Key Base64 인코딩
     */
    private String encodeSecretKey() {
        return Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
    }

    protected abstract PayMethod getTossPayMethod();

    /**
     * 결제수단별 응답 메시지 파싱
     */
    protected abstract PgPayResponse convertToResponse(TossTransResponse response);

    /**
     * 결제수단별 취소 요청 body 생성
     */
    protected abstract void generateCancelRequest(Map<String, Object> commonBody, PayCancelCommand command);
}

