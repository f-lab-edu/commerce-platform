package domain.aggregate;

import domain.enums.PayMethod;
import domain.enums.TransactionStatus;
import domain.enums.PgProvider;
import domain.vo.PaymentResult;
import domain.vo.PgRequest;
import domain.vo.PgResponse;
import vo.Money;

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
