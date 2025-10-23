package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.enums.PayMethod;
import com.commerce.platform.core.domain.enums.PgProvider;
import com.commerce.platform.core.domain.enums.TransactionStatus;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.PaymentResult;
import com.commerce.platform.core.domain.vo.PgRequest;
import com.commerce.platform.core.domain.vo.PgResponse;

import java.time.LocalDateTime;

public class Payment {
    private String paymentId;

    // 참조
    private String orderId;
    private String customerId;
    private String merchantId;

    // 결제정보
    private TransactionStatus transactionStatus;
    private PayMethod payMethod;
    private Money amount;
    private String installment; // todo 타입?
    private LocalDateTime paymentDateTime;

    // pg정보
    private PgProvider pgProvider;
    private PgRequest pgRequest;   // pg사 결제 요청
    private PgResponse pgResponse; // pg사 결제 응답

    // 우리가 제공할 응답
    private PaymentResult paymentResult;


    // 승인, 취소, 부분취소
}
