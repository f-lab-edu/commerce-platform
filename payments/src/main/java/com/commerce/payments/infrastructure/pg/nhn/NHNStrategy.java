package com.commerce.payments.infrastructure.pg.nhn;

import com.commerce.payments.core.application.port.in.dto.PayCancelCommand;
import com.commerce.payments.core.application.port.in.dto.PayOrderCommand;
import com.commerce.payments.core.application.port.out.PgStrategy;
import com.commerce.payments.core.domain.vo.payments.PgPayCancelResponse;
import com.commerce.payments.core.domain.vo.payments.PgPayResponse;
import com.commerce.payments.core.domain.enums.PayMethod;
import com.commerce.payments.core.domain.enums.PaymentStatus;
import com.commerce.payments.core.domain.enums.PgProvider;
import com.commerce.payments.infrastructure.pg.nhn.dto.NhnApprovalRequest;
import com.commerce.payments.infrastructure.pg.nhn.dto.NhnApprovalResponse;
import com.commerce.payments.infrastructure.pg.nhn.dto.NhnCancelRequest;
import com.commerce.payments.infrastructure.pg.nhn.dto.NhnCancelResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 결제유형별 특정 파라미터 값 구분 필요.
 *         응답 개별 파싱 필요
 *
 * 여기서 필요에 따라 NHN의 결제수단별 프로세스를 추상화한다.
 */
@Component
@RequiredArgsConstructor
public abstract class NHNStrategy<T extends NhnApprovalResponse> extends PgStrategy {
    private final RestTemplate restTemplate;
    private static final String NHN_CONFIRM_URL = "https://stg-spl.kcp.co.kr/gw/enc/v1/payment";
    private static final String NHN_CANCEL_URL = "https://stg-spl.kcp.co.kr/gw/mod/v1/cancel";
    private final String SITE_CD = "T0000";     // 상점코드

    /**
     * 공통 승인 요청
     */
    @Override
    public PgPayResponse processApproval(PayOrderCommand command) {
        // 승인 요청객체 생성
        NhnApprovalRequest request = NhnApprovalRequest.builder()
                .payType(getNhnPayType())  // 결제유형별 값 추출
                .tranCd("00100000")
                .ordrNo(String.valueOf(command.getOrderId().id()))
                .ordrMony(String.valueOf(command.getApprovedAmount().value()))
                .build();

        // API 호출
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<NhnApprovalRequest> httpRequest =
                new HttpEntity<>(request, headers);

        // 결제유형별 응답 파싱 가능하도록 함
        T response = restTemplate.exchange(
                NHN_CONFIRM_URL,
                HttpMethod.POST,
                httpRequest,
                getResponseType()
        ).getBody();

        // 결제유형별 응답 생성
        return convertToResponse(response);
    }


    /**
     * 공통 취소 요청
     */
    @Override
    public PgPayCancelResponse processCancel(PayCancelCommand command) {
        // API 호출
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        NhnCancelRequest cancelRequest = NhnCancelRequest.builder()
                .site_cd(SITE_CD)
                .tno(command.getPgTid())
                .mod_type(command.getPaymentStatus().equals(PaymentStatus.PARTIAL_CANCELED)
                        ? "STPC"        // 부분취소 코드
                        : "STSC")       // 전체취소
                .build();

        HttpEntity<NhnCancelRequest> httpRequest =
                new HttpEntity<>(cancelRequest, headers);

        NhnCancelResponse response = restTemplate.exchange(
                NHN_CANCEL_URL,
                HttpMethod.POST,
                httpRequest,
                NhnCancelResponse.class
        ).getBody();

        return new PgPayCancelResponse(
                response.tno(),
                response.resCd(),
                response.resMsg(),
                null,
                Long.parseLong(response.cardModMny()),
                "0000".equals(response.resCd()) ? true : false
        );
    }

    public PgProvider getPgProvider() {
        return PgProvider.NHN;
    }

    @Override
    public PayMethod getPgPayMethod() {
        return getNhnPayMethod();
    }

    @Override
    public Object initPayment() {
        return null;
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
     * 결제 응답 글래스 제공
     * */
    protected abstract Class<T> getResponseType();

    /**
     * 결제수단별 승인 응답 메시지 파싱
     */
    protected abstract PgPayResponse convertToResponse(T response);

}
