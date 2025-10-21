package domain.vo;

import domain.enums.TransactionStatus;
import vo.Money;

/**
 * pg 결제 요청 시 필요 데이터
 */
public record PgRequest(
        String mid, // pg사에서 관리하는 우리의 id 로 휴대폰/카드/간편결제 화면을 보여줄거임

        // 주문정보
        String orderId,

        // 결제 정보
        TransactionStatus status,
        Money amount,
        String installment,

        // 상품정보
        String productName

) {
}
