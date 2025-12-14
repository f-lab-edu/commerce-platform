package com.commerce.payments.infrastructure.pg.toss;

import com.commerce.payments.PgPayResponse;
import com.commerce.payments.application.port.in.command.PayCancelCommand;
import com.commerce.payments.domain.enums.PayMethod;
import com.commerce.payments.infrastructure.pg.toss.dto.TossTransResponse;
import com.commerce.shared.vo.Money;
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
                .amount(Money.of(response.totalAmount()))
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
