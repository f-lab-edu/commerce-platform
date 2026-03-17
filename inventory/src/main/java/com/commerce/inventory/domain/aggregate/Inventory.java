package com.commerce.inventory.domain.aggregate;

import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 재고 도메인 엔티티
 */
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "inventory")
@Entity
public class Inventory {
    @EmbeddedId
    private ProductId productId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "quantity"))
    private Quantity quantity;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 재고 차감
     */
    public void decreaseQuantity(long amount) {
        this.quantity = this.quantity.minus(Quantity.create(amount));
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 재고 증가 (취소/반품)
     */
    public void increaseQuantity(long amount) {
        this.quantity = this.quantity.add(Quantity.create(amount));
        this.updatedAt = LocalDateTime.now();
    }
}
