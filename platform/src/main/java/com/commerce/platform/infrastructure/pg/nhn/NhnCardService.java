package com.commerce.platform.infrastructure.pg.nhn;

import com.commerce.platform.core.application.out.dto.PgPayCancelResponse;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.infrastructure.pg.nhn.dto.NhnApprovalResponse;
import com.commerce.platform.infrastructure.pg.nhn.dto.NhnCancelResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NhnCardService extends NHNStrategy{
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
    protected PgPayResponse convertToResponse(NhnApprovalResponse response) {
        boolean isSuccess = "0000".equals(response.resCd());

        return new PgPayResponse(
                response.tno(),
                response.resCd(),
                response.resMsg(),
                Money.create(Long.parseLong(response.amount())),
                isSuccess,
                new PgPayResponse.Card(
                        response.appNo(),
                        response.cardCd(),
                        response.cardBinType02()
                ),
                null,
                null
        );
    }

    @Override
    protected PgPayCancelResponse convertCancelToResponse(NhnCancelResponse response) {
        return new PgPayCancelResponse(
                response.tno(),
                response.resCd(),
                response.resMsg(),
                null,
                Long.parseLong(response.cardModMny()),
                "0000".equals(response.resCd()) ? true : false
        );
    }
}
