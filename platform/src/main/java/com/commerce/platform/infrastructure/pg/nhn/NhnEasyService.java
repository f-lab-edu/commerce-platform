package com.commerce.platform.infrastructure.pg.nhn;

import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.infrastructure.pg.nhn.dto.NhnEasyCardApprovalResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NhnEasyService extends NHNStrategy<NhnEasyCardApprovalResponse> {
    public NhnEasyService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected PayMethod getNhnPayMethod() {
        return PayMethod.EASY_PAY;
    }

    @Override
    protected String getNhnPayType() {
        return "PACA";
    }

    @Override
    protected Class<NhnEasyCardApprovalResponse> getResponseType() {
        return NhnEasyCardApprovalResponse.class;
    }

    @Override
    protected PgPayResponse convertToResponse(NhnEasyCardApprovalResponse response) {
        boolean isSuccess = "0000".equals(response.resCd());

        return PgPayResponse.builder()
                .pgTid(response.tno())
                .responseCode(response.resCd())
                .responseMessage(response.resMsg())
                .amount(Money.create(Long.parseLong(response.amount())))
                .isSuccess(isSuccess)
                .easyPay(PgPayResponse.EasyPay.builder()
                        .provider(response.cardOtherPayType())
                        .build())
                .build();
    }

}
