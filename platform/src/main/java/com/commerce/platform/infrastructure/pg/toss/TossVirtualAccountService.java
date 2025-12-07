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
        return new PgPayResponse(
                response.paymentKey(),
                response.status(),
                response.status(),
                Money.create(response.totalAmount()),
                isSuccess,
                null,
                null,
                new PgPayResponse.VirtualAccount(
                        virtualAccountInfo.accountType(),
                        virtualAccountInfo.accountNumber(),
                        virtualAccountInfo.bankCode(),
                        virtualAccountInfo.customerName(),
                        virtualAccountInfo.dueDate()
                )
        );
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
