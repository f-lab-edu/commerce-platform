package com.commerce.platform.infrastructure.pg.toss;

import com.commerce.platform.core.application.in.dto.PayCancelCommand;
import com.commerce.platform.core.application.out.dto.PgPayResponse;
import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.infrastructure.pg.toss.dto.TossTransResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TossCardService extends TossStrategy{
    public TossCardService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected PayMethod getTossPayMethod() {
        return PayMethod.CARD;
    }

    @Override
    protected PgPayResponse convertToResponse(TossTransResponse response) {
        boolean isSuccess = "DONE".equals(response.status());

        TossTransResponse.CardInfo cardResponse = response.card();
        return PgPayResponse.builder()
                .pgTid(response.paymentKey())
                .responseCode(response.status())
                .responseMessage(response.status())
                .amount(Money.create(response.totalAmount()))
                .isSuccess(isSuccess)
                .card(PgPayResponse.Card.builder()
                        .approveNo(cardResponse.approveNo())
                        .issuerCode(cardResponse.issuerCode())
                        .cardType(cardResponse.cardType())
                        .build())
                .build();
    }

    @Override
    protected void generateCancelRequest(Map<String, Object> commonBody, PayCancelCommand command) {
        // 추가 세팅 없음
    }
}
