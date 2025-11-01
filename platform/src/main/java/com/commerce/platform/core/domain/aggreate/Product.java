package com.commerce.platform.core.domain.aggreate;

import com.commerce.platform.core.domain.enums.ProductStatus;
import com.commerce.platform.core.domain.vo.Money;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.platform.core.domain.vo.Quantity;
import com.commerce.platform.shared.exception.BusinessException;
import lombok.Builder;
import lombok.Getter;

import static com.commerce.platform.core.domain.enums.ProductStatus.DISCONTINUED;
import static com.commerce.platform.core.domain.enums.ProductStatus.OUT_OF_STOCK;
import static com.commerce.platform.shared.exception.BusinessError.PRODUCT_NOT_AVAILABLE;

@Getter
@Builder(toBuilder = true)
public class Product {
    private ProductId productId;
    private String productName;
    private String description;
    private Money price;
    private Quantity stockQuantity;
    private ProductStatus status;

    /**
     * 요청값으로 재고 변경 및 ProductStatus 세팅
     * @param updatedQuantity
     * @return
     */
    public void changeStockQuantity(Quantity updatedQuantity) {
        this.stockQuantity = updatedQuantity;
        this.status = ProductStatus.fromStockQuantity(updatedQuantity);
    }

    /**
     * 재고 추가
     * @param incQuantity
     * @return
     * @throws Exception
     */
    public void increaseStock(Quantity incQuantity) {
        Quantity result = this.stockQuantity.add(incQuantity);
        changeStockQuantity(result);
    }

    /**
     * 재고 소진
     * @param decQuantity
     * @return
     * @throws Exception
     */
    public void decreaseStock(Quantity decQuantity) {
        if(this.status == OUT_OF_STOCK
                || this.status == DISCONTINUED ) {
            throw new BusinessException(PRODUCT_NOT_AVAILABLE);
        }

        Quantity result =  this.stockQuantity.minus(decQuantity);
        changeStockQuantity(result);
    }
}
