package domain.aggregate;

import domain.enums.ProductStatus;
import lombok.Getter;
import vo.Money;

import java.util.UUID;

@Getter
public class Product {
    private String productId;
    private String productName;
    private Money price;
    public ProductStatus status;

    /**
     * 상품 등록
     */
    public static Product create(
            String productName,
            long price) {
        Product product = new Product();
        product.productId = String.valueOf(UUID.randomUUID());
        product.productName = productName;
        product.price = new Money(price);
        product.status = ProductStatus.ACTIVE;

        return product;
    }
}
