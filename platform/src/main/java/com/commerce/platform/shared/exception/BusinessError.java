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

    // Order 관련
    INVALID_ORDER_ID("O002", "유효하지 않는 주문 ID"),

    // Quantity 관련
    INVALID_QUANTITY("Q001", "유효하지 않은 수량입니다"),
    QUANTITY_BELOW_MINIMUM("Q002", "수량은 최소 1개 이상이어야 합니다"),
    QUANTITY_EXCEEDS_MAXIMUM("Q003", "수량이 최대 한도를 초과했습니다"),

    // Stock 관련
    INSUFFICIENT_STOCK("S001", "재고가 부족합니다"),
    STOCK_NOT_AVAILABLE("S002", "재고를 사용할 수 없습니다"),

    // Customer
    INVALID_CUSTOMER("M001", "고객ID 확인요망"),

    // Coupon
    INVALID_COUPON("C001", "유효하지 않는 쿠폰 ID"),

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