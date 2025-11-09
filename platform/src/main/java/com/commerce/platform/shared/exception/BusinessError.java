package com.commerce.platform.shared.exception;

import lombok.Getter;

/**
 * 오류코드 관리
 */
@Getter
public enum BusinessError {

    // Product 관련
    PRODUCT_NOT_FOUND("P001", "상품을 찾을 수 없습니다"),
    INVALID_PRODUCT_ID("P002", "유효하지 않는 상품 ID"),
    PRODUCT_NOT_AVAILABLE("P002", "구매가 불가능한 상품입니다."),

    // Order 관련
    INVALID_ORDER_ID("O001", "유효하지 않는 주문 ID"),

    // Quantity 관련
    INVALID_QUANTITY("Q001", "유효하지 않은 수량입니다"),
    QUANTITY_BELOW_MINIMUM("Q002", "수량은 최소 1개 이상이어야 합니다"),
    QUANTITY_EXCEEDS_MAXIMUM("Q003", "수량이 최대 한도를 초과했습니다"),

    // Stock 관련
    INSUFFICIENT_STOCK("S001", "재고가 부족합니다"),
    STOCK_NOT_AVAILABLE("S002", "재고를 사용할 수 없습니다"),

    // Customer
    INVALID_CUSTOMER("M001", "고객ID 확인요망"),
    DUPLICATED_REGISTRY_CARD("M002", "이미 등록된 카드 존재"),
    EXCEED_REGISTRY_CARD("M003", "카드는 최대 5개 등록 가능합니다."),
    NOT_FOUND_REGISTRY_CARD("M004", "해당 카드가 존재하지 않습니다."),

    // Coupon
    INVALID_COUPON("C001", "유효하지 않는 쿠폰 ID"),
    BELOW_LEAST_ORDER_AMT("C002", "최소주문금액 미달"),
    NOT_WITHIN_PERIOD_COUPON("C003", "쿠폰 발급 기간이 아닙니다."),
    QUANTITY_EXCEEDED_COUPON("C004", "발급 가능 수량 초과"),

    // CouponIssues
    NOT_ISSUED_COUPON("I001", "미발행된 쿠폰입니다."),
    USED_ISSUED_COUPON("I002", "이미 사용된 쿠폰입니다."),
    EXPIRED_ISSUED_COUPON("I003", "사용 만료된 쿠폰입니다."),
    DUPLICATE_ISSUED_COUPON("I004", "이미 발급된 쿠폰입니다."),

    // Payment
    INVALID_PAYMENT("T001", "유효하지 않는 결제ID"),
    PAYMENT_ALREADY_CANCELED("T002", "이미 전체 취소된 결제입니다"),
    PAYMENT_HAS_PARTIAL_CANCEL("T003", "부분 취소 내역이 존재하여 전체 취소가 불가능합니다"),
    PAYMENT_CANCEL_AMOUNT_EXCEEDED("T004", "취소 가능 금액을 초과했습니다"),
    PAYMENT_INVALID_STATUS_FOR_CANCEL("T005", "취소 불가능한 결제 상태입니다"),
    PG_RESPONSE_FAILED("T006", "결제처리 결과 실패"),
    INVALID_PARTIAL_CANCEL_AMOUNT("T007", "부분취소 불가능한 금액입니다. 전체취소로 요청하세요."),

    // Money
    INVALID_MONEY("M001", "금액 오류"),

    // ValidPeriod
    INVALID_PERIOD("V001", "유효기간 오류"),

    // @Valid, 미지정오류
    INVALID_REQUEST_VALUE("8888", "요청데이터를 확인하세요"),
    UNKNOWN_ERROR("9999", "관리자에서 문의해주세요.")
    ;

    private final String code;
    private final String message;

    BusinessError(String code, String message) {
        this.code = code;
        this.message = message;
    }

}