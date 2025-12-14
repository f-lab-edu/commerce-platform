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
public class TossVirtualAccountService extends TossStrategy{

    public TossVirtualAccountService(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    protected PayMethod getTossPayMethod() {
        return PayMethod.VIRTUAL_ACCOUNT;
    }

    @Override
    protected PgPayResponse convertToResponse(TossTransResponse response) {
        boolean isSuccess = "DONE".equals(response.status())
                || "WAITING_FOR_DEPOSIT".equals(response.status());

        TossTransResponse.VirtualAccountInfo virtualAccountInfo = response.virtualAccount();
        return PgPayResponse.builder()
                .pgTid(response.paymentKey())
                .responseCode(response.status())
                .responseMessage(response.status())
                .amount(Money.of(response.totalAmount()))
                .isSuccess(isSuccess)
                .virtualAccount(PgPayResponse.VirtualAccount.builder()
                        .accountType(virtualAccountInfo.accountType())
                        .accountNumber(virtualAccountInfo.accountNumber())
                        .bankCode(virtualAccountInfo.bankCode())
                        .depositorName(virtualAccountInfo.customerName())
                        .dueDate(virtualAccountInfo.dueDate())
                        .build())
                .build();
    }

    @Override
    protected void generateCancelRequest(Map<String, Object> commonBody, PayCancelCommand command) {
        PayCancelCommand.RefundReceiveAccount refundAccount = command.getRefundReceiveAccount();

        Map<String, String> refundInfo = Map.of(
                "bank", refundAccount.getBankCode(),
                "accountNumber", refundAccount.getAccountNumber(),
                "holderName", refundAccount.getHolderName()
        );

        commonBody.put("refundReceiveAccount", refundInfo);
    }
}
