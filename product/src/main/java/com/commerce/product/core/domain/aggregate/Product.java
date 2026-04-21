package com.commerce.product.core.domain.aggregate;

import com.commerce.product.core.domain.enums.ProductStatus;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product")
@Entity
public class Product {
    @EmbeddedId
    private ProductId productId;

    @Column(name = "name", nullable = false, length = 50)
    private String productName;

    @Column(name = "description", length = 200)
    private String description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "price"))
    private Money price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 4)
    private ProductStatus status;

    @Builder(toBuilder = true)
    private Product(ProductId productId, String productName, String description, Money price, ProductStatus status) {
        this.productId = productId;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.status = status;
    }
}
