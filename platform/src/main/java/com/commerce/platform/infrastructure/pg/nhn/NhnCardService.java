package com.commerce.platform.infrastructure.pg.nhn;

import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.infrastructure.pg.nhn.dto.NhnCardApprovalResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NhnCardService extends NHNStrategy<NhnCardApprovalResponse>{
    public NhnCardService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected PayMethod getNhnPayMethod() {
        return PayMethod.CARD;
    }

    @Override
    protected String getNhnPayType() {
        return "PACA";
    }

    @Override
    protected Class<NhnCardApprovalResponse> getResponseType() {
        return NhnCardApprovalResponse.class;
    }

    @Override
    protected PgPayResponse convertToResponse(NhnCardApprovalResponse response) {
        boolean isSuccess = "0000".equals(response.resCd());

        return PgPayResponse.builder()
                .pgTid(response.tno())
                .responseCode(response.resCd())
                .responseMessage(response.resMsg())
                .amount(Money.create(Long.parseLong(response.amount())))
                .isSuccess(isSuccess)
                .card(PgPayResponse.Card.builder()
                        .approveNo(response.appNo())
                        .issuerCode(response.cardCd())
                        .cardType( response.cardBinType02())
                        .build())
                .build();
    }

}
