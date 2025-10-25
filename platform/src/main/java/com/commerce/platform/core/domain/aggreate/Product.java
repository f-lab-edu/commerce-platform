package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.bootstrap.dto.product.CreateProductRequest;
import com.commerce.platform.core.domain.enums.ProductStatus;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.platform.core.domain.vo.Quantity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(access = AccessLevel.PRIVATE, toBuilder = true)
public class Product {
    private ProductId productId;
    private String productName;
    private String description;
    private Money price;
    private Quantity stockQuantity;
    private ProductStatus status;

    /**
     * 상품 등록
     */
    public static Product from(CreateProductRequest request) {
        Quantity quantity = Quantity.create(request.stockQuantity());

        return Product.builder()
                .productId(ProductId.create())
                .productName(request.name())
                .description(request.description())
                .price(Money.create(request.price()))
                .stockQuantity(quantity)
                .status(ProductStatus.fromStockQuantity(quantity))
                .build();
    }

    /**
     * 요청값으로 재고 변경 및 ProductStatus 세팅
     * @param updatedQuantity
     * @return
     */
    public Product changeStockQuantity(Quantity updatedQuantity) {
        return this.toBuilder()
                .stockQuantity(updatedQuantity)
                .status(ProductStatus.fromStockQuantity(updatedQuantity))
                .build();
    }

    /**
     * 재고 추가
     * @param incQuantity
     * @return
     * @throws Exception
     */
    public Product increaseStock(Quantity incQuantity) throws Exception {
        Quantity result = this.stockQuantity.add(incQuantity);
        return changeStockQuantity(result);
    }

    /**
     * 재고 소진
     * @param decQuantity
     * @return
     * @throws Exception
     */
    public Product decreaseStock(Quantity decQuantity) throws Exception {
        Quantity result =  this.stockQuantity.minus(decQuantity);
        return changeStockQuantity(result);
    }
}
