package com.commerce.platform.infrastructure.pg.toss.dto;

import java.util.List;

/**
 * TOSS 결제유형별 요청 응답 DTO
 **/
public record TossTransResponse(
        String mId,                         // 가맹점 ID (상점 식별자, MID)
        String version,                     // API 버전 (예: "2022-11-16")
        String paymentKey,                  // 결제 키 (토스가 발급한 결제 고유 식별자, 승인/취소/조회 시 필수)
        String orderId,                     // 주문 ID (상점에서 생성한 주문 고유 번호)
        String orderName,                   // 주문명 (예: "토스 티셔츠 외 2건", 결제창에 표시됨)
        String method,                      // 결제수단 ("카드", "가상계좌", "간편결제", "휴대폰" 등)
        Long totalAmount,                   // 총 결제 금액 (원 단위, 최초 결제 요청 금액)
        Long balanceAmount,                 // 취소 후 남은 금액 (부분 취소 시 감소, 전액 취소 시 0)
        String status,                      // 결제 상태 (READY, IN_PROGRESS, WAITING_FOR_DEPOSIT, DONE, CANCELED, PARTIAL_CANCELED, ABORTED, EXPIRED)
        String requestedAt,                 // 결제 요청 시각 (ISO 8601 형식: 2024-12-01T10:00:00+09:00)
        String approvedAt,                  // 결제 승인 시각 (ISO 8601 형식, 승인 완료 후에만 존재)
        CardInfo card,                      // 카드 결제 정보 (카드 결제 시에만 존재, 나머지 null)
        EasyPayInfo easyPay,                // 간편결제 정보 (간편결제 시에만 존재, 나머지 null)
        VirtualAccountInfo virtualAccount,  // 가상계좌 정보 (가상계좌 결제 시에만 존재, 나머지 null)
        List<CancelInfo> cancels            // 취소 정보 리스트 (취소 발생 시 배열에 누적, 없으면 빈 배열 또는 null)
) {
    /**
     * 카드결제 정보
     */
    public record CardInfo(
            String issuerCode,              // 발급사 코드 (카드를 발행한 회사)
            String acquirerCode,            // 매입사 코드 (가맹점과 계약한 카드사)
            String number,                  // 마스킹된 카드번호 (앞 8자리, 뒤 1자리만 표시)
            Integer installmentPlanMonths,  // 할부 개월 (0=일시불, 2~12=할부)
            String cardType,                // 카드 타입 (신용/체크/기프트)
            String ownerType,               // 소유자 구분 (개인/법인)
            String approveNo                // 승인번호 (PG사가 아닌 카드사에서 발급)
    ) {}

    /**
     * 간편결제 정보
     */
    public record EasyPayInfo(
            String provider,                // 간편결제 제공자 (토스페이, 네이버페이, 카카오페이 등)
            String amount,                  // 실제 결제된 금액
            String discountAmount           // 간편결제 할인 금액 (프로모션 적용 시)
    ) {}

    /**
     * 가상계좌 정보
     */
    public record VirtualAccountInfo(
            String accountNumber,           // 발급된 가상계좌 번호 (구매자가 입금할 계좌)
            String bankCode,                // 은행 코드 (20=우리, 004=KB, 011=NH농협)
            String customerName,            // 입금자명 (주문 시 입력한 구매자 이름)
            String dueDate,                 // 입금 기한 (이 시간까지 입금해야 함)
            String accountType,             // 계좌 유형 (일반=주문마다 새 계좌, 고정=항상 동일 계좌)
            Boolean expired,                // 만료 여부 (true=입금 기한 지남)
            String settlementStatus,        // 정산 상태 (INCOMPLETED=미정산, COMPLETED=정산완료)
            String refundStatus             // 환불 처리 상태 (취소 시 환불 진행 현황)
    ) {}

    /**
     * 결제 취소 정보
     * - Payment 객체의 cancels 필드에 배열로 담겨서 반환됨
     * - 부분 취소를 여러 번 하면 배열에 여러 개의 취소 객체가 쌓임
     */
    public record CancelInfo(
            String transactionKey,         // 취소 거래 키 (각 취소마다 고유)
            String cancelReason,           // 취소 사유
            Long cancelAmount,             // 취소 금액
            Long taxFreeAmount,            // 취소된 면세 금액
            Long taxExemptionAmount,       // 과세 제외 금액 (복지, 교육비 등)
            Long refundableAmount,         // 남은 취소 가능 금액
            Long easyPayDiscountAmount,    // 간편결제 할인 금액
            String canceledAt,             // 취소 처리 시각 (ISO 8601)
            String cancelStatus,           // 취소 상태 (DONE, FAILED, IN_PROGRESS 등)
            String receiptKey              // 취소 영수증 키
    ) {}
}
