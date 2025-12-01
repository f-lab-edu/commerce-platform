package com.commerce.platform.infrastructure.pg.toss;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.application.out.dto.PgPayCancelResponse;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PgProvider;
import com.commerce.platform.core.domain.vo.Money;
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

/**
 * TOSS PG
 * 카드, 간편결제, 가상계좌 에 대해 동일한 승인/취소 API 사용
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossStrategy extends PgStrategy {

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
        TossApprovalResponse response = callTossConfirmApi(
                command.getJsonSubData(),
                command.getOrderId().id(),
                command.getApprovedAmount().value()
        );

        return convertToResponse(command.getPayMethod(), response);
    }

    /**
     * TOSS 결제 취소 처리
     * 카드, 간편결제, 가상계좌 모두 동일한 취소 API 사용
     * @param command  취소요청dto
     * @return toss -> 공통dto 변환
     */
    @Override
    public PgPayCancelResponse processCancel(PayCancelCommand command) {
        TossCancelResponse tossCancelResponse = callTossCancelApi(
                command.getPgTid(),
                command.getCanceledAmount().value(),
                command.getCancelReason(),
                command.getRefundReceiveAccount()
        );

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
     * 승인 API 호출
     */
    private TossApprovalResponse callTossConfirmApi(
            String paymentKey,
            String orderId,
            Long amount
    ) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodeSecretKey());

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
                    TossApprovalResponse.class
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
     *
     * @param paymentKey 결제 키
     * @param cancelAmount 취소 금액 (null이면 전액 취소)
     * @param cancelReason 취소 사유
     * @param refundAccount 환불 계좌 정보 (가상계좌 입금 후 취소 시 필수, 그 외 null)
     */
    private TossCancelResponse callTossCancelApi(
            String paymentKey,
            Long cancelAmount,
            String cancelReason,
            PayCancelCommand.RefundReceiveAccount refundAccount
    ) {
        try {
            String url = TOSS_CANCEL_URL + paymentKey + "/cancel";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodeSecretKey());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("cancelReason", cancelReason);

            // 부분 취소인 경우 금액 추가
            if (cancelAmount != null) {
                requestBody.put("cancelAmount", cancelAmount);
            }

            // 가상계좌 환불 계좌 정보 추가
            if (refundAccount != null) {
                Map<String, String> refundInfo = new HashMap<>();
                refundInfo.put("bank", refundAccount.getBankCode());
                refundInfo.put("accountNumber", refundAccount.getAccountNumber());
                refundInfo.put("holderName", refundAccount.getHolderName());
                requestBody.put("refundReceiveAccount", refundInfo);
            }

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<TossApprovalResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    TossApprovalResponse.class
            );

            TossApprovalResponse.CancelInfo lastCancel = response.getBody()
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

    /**
     * 결제수단별 응답 메시지 생성
     */
    private PgPayResponse convertToResponse(PayMethod payMethod, TossApprovalResponse response) {
        // toss 에서 정상서리된 경우 상태값
        boolean isSuccess = "DONE".equals(response.status())
                || "WAITING_FOR_DEPOSIT".equals(response.status());

        return switch (payMethod) {
            case CARD -> {
                TossApprovalResponse.CardInfo cardResponse = response.card();
                yield new PgPayResponse(
                        response.paymentKey(),
                        response.status(),
                        response.status(),
                        Money.create(response.totalAmount()),
                        isSuccess,
                        new PgPayResponse.Card(
                                cardResponse.approveNo(),
                                cardResponse.issuerCode(),
                                cardResponse.cardType()
                        ),
                        null,
                        null
                );
            }
            case VIRTUAL_ACCOUNT -> {
                TossApprovalResponse.VirtualAccountInfo virtualAccountInfo = response.virtualAccount();
                yield new PgPayResponse(
                        response.paymentKey(),
                        response.status(),
                        response.status(),
                        Money.create(response.totalAmount()),
                        isSuccess,
                        null,
                        null,
                        new PgPayResponse.VirtualAccount(
                                virtualAccountInfo.accountType(),
                                virtualAccountInfo.accountNumber(),
                                virtualAccountInfo.bankCode(),
                                virtualAccountInfo.customerName(),
                                virtualAccountInfo.dueDate()
                        )
                );
            }
            case EASY_PAY -> {
                TossApprovalResponse.EasyPayInfo easyPayInfo = response.easyPay();
                yield new PgPayResponse(
                        response.paymentKey(),
                        response.status(),
                        response.status(),
                        Money.create(response.totalAmount()),
                        isSuccess,
                        null,
                        new PgPayResponse.EasyPay(
                                easyPayInfo.provider(),
                                easyPayInfo.amount(),
                                easyPayInfo.discountAmount()
                        ),
                        null
                );
            }
            default -> null;
        };
    }

    /**
     * Secret Key Base64 인코딩
     */
    private String encodeSecretKey() {
        return Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
    }
}

