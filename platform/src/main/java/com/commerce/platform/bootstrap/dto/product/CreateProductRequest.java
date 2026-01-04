package com.commerce.platform.bootstrap.dto.product;

import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.enums.ProductStatus;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.shared.vo.Money;
import com.commerce.shared.vo.Quantity;
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
        /**
         * 상품 등록
         */
        public static Product to(CreateProductRequest request) {
                Quantity quantity = Quantity.create(request.stockQuantity());

                return Product.builder()
                        .productId(ProductId.create())
                        .productName(request.name())
                        .description(request.description())
                        .price(Money.of(request.price()))
                        .stockQuantity(quantity)
                        .status(ProductStatus.fromStockQuantity(quantity))
                        .build();
        }
}
