package com.commerce.platform.infrastructure.pg.nhn;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.in.dto.PayOrderCommand;
import com.commerce.platform.core.application.out.PgStrategy;
import com.commerce.platform.core.application.out.dto.PgPayCancelResponse;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PgProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 카드결제인 경우 요청 동일하다.
 */
@Component
@RequiredArgsConstructor
public class NHNStrategy extends PgStrategy {
    private final RestTemplate restTemplate;
    private static final String NHN_CONFIRM_URL = "https://stg-spl.kcp.co.kr/gw/enc/v1/payment";
    private static final String NHN_CANCEL_URL = "https://stg-spl.kcp.co.kr/gw/mod/v1/cancel";


    @Override
    public PgPayResponse processApproval(PayOrderCommand command) {
        // 승인 요청객체 생성
        NhnApprovalRequest request = NhnApprovalRequest.create(command);

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

        return convertToResponse(response);
    }

    @Override
    public PgPayCancelResponse processCancel(PayCancelCommand command) {
        return null;
    }

    public PgProvider getPgProvider() {
        return PgProvider.NHN;
    }

    private PgPayResponse convertToResponse(NhnApprovalResponse response) {
        // todo
        return null;
    }
}
