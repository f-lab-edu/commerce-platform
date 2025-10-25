package com.commerce.platform.bootstrap.dto.product;

import jakarta.validation.constraints.*;

public record CreateProductRequest(
        @NotBlank(message = "상품명은 필수입니다")
        @Size(max = 50, message = "상품명은 50자를 초과할 수 없습니다")
        String name,

        @Size(max = 200, message = "상품 설명은 200자를 초과할 수 없습니다")
        String description,

        @NotNull(message = "가격은 필수입니다")
        @Min(value = 100, message = "가격은 100원 이상이어야 합니다")
        Long price,

        @NotNull(message = "재고 수량은 필수입니다")
        @Min(value = 1, message = "재고 수량은 1 이상이어야 합니다")
        Long stockQuantity
) {
}
