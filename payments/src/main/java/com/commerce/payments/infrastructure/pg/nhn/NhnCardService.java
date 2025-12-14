package com.commerce.payments.infrastructure.pg.nhn;

import com.commerce.payments.PgPayResponse;
import com.commerce.payments.domain.enums.PayMethod;
import com.commerce.payments.infrastructure.pg.nhn.dto.NhnCardApprovalResponse;
import com.commerce.shared.vo.Money;
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
                .amount(Money.of(Long.parseLong(response.amount())))
                .isSuccess(isSuccess)
                .card(PgPayResponse.Card.builder()
                        .approveNo(response.appNo())
                        .issuerCode(response.cardCd())
                        .cardType( response.cardBinType02())
                        .build())
                .build();
    }

}
