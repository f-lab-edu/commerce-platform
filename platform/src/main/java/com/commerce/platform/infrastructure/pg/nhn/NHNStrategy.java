package com.commerce.platform.infrastructure.pg.nhn;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.application.out.dto.PgPayCancelResponse;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PaymentStatus;
import com.commerce.platform.core.domain.enums.PgProvider;
import com.commerce.platform.infrastructure.pg.nhn.dto.NhnApprovalRequest;
import com.commerce.platform.infrastructure.pg.nhn.dto.NhnApprovalResponse;
import com.commerce.platform.infrastructure.pg.nhn.dto.NhnCancelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 결제유형별 특정 파라미터 값 구분 필요.
 *         응답 개별 파싱 필요
 */
@Component
@RequiredArgsConstructor
public abstract class NHNStrategy extends PgStrategy {
    private final RestTemplate restTemplate;
    private static final String NHN_CONFIRM_URL = "https://stg-spl.kcp.co.kr/gw/enc/v1/payment";
    private static final String NHN_CANCEL_URL = "https://stg-spl.kcp.co.kr/gw/mod/v1/cancel";
    private final String SITE_CD = "T0000";     // 상점코드

    @Override
    public PgPayResponse processApproval(PayOrderCommand command) {
        // 승인 요청객체 생성
        NhnApprovalRequest request = new NhnApprovalRequest(
                command.getJsonSubData(),
                command.getJsonSubData(),
                command.getJsonSubData(),
                command.getJsonSubData(),
                "00100000",
                String.valueOf(command.getApprovedAmount().value()),
                String.valueOf(command.getOrderId().id()),
                getNhnPayType()         // 결제유형별 값 추출
        );

        // API 호출
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<NhnApprovalRequest> httpRequest =
                new HttpEntity<>(request, headers);

        NhnApprovalResponse response = restTemplate.exchange(
                NHN_CONFIRM_URL,
                HttpMethod.POST,
                httpRequest,
                NhnApprovalResponse.class
        ).getBody();

        // 결제유형별 응답 파싱
        return convertToResponse(response);
    }

    @Override
    public PgPayCancelResponse processCancel(PayCancelCommand command) {
        // API 호출
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("site_cd", SITE_CD);
        requestBody.put("tno", command.getPgTid());
        requestBody.put("kcp_cert_info", "tmp_cert_info");
        requestBody.put("kcp_sign_data", "tmp_cert_data");
        requestBody.put("mod_type",
                command.getPaymentStatus().equals(PaymentStatus.PARTIAL_CANCELED)
                        ? "STPC"        // 부분취소 코드
                        : "STSC");      // 전체취소 코드

        HttpEntity<Map<String, Object>> httpRequest =
                new HttpEntity<>(requestBody, headers);

        NhnCancelResponse response = restTemplate.exchange(
                NHN_CANCEL_URL,
                HttpMethod.POST,
                httpRequest,
                NhnCancelResponse.class
        ).getBody();

        // 결제수단별 취소 응답 메시지 파싱
        return convertCancelToResponse(response);
    }

    public PgProvider getPgProvider() {
        return PgProvider.NHN;
    }

    @Override
    public PayMethod getPgPayMethod() {
        return getNhnPayMethod();
    }


    /**
     * NHN 구현체 중 특정 결제서비스 빈 추출을 위함
     * @return
     */
    protected abstract PayMethod getNhnPayMethod();

    /**
     * NHN 는 요청 시 결제유형별 pay_type값이 상이함.
     * @return
     */
    protected abstract String getNhnPayType();

    /**
     * 결제수단별 승인 응답 메시지 파싱
     */
    protected abstract PgPayResponse convertToResponse(NhnApprovalResponse response);

    /**
     * 결제수단별 취소 응답 메시지 파싱
     */
    protected abstract PgPayCancelResponse convertCancelToResponse(NhnCancelResponse response);
}
