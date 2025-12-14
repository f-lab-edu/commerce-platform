package com.commerce.platform.core.domain.service;

import com.commerce.platform.core.application.in.ProductUseCaseImpl;
import com.commerce.platform.core.application.in.dto.ProductDetail;
import com.commerce.platform.core.application.out.ProductOutputPort;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.enums.ProductStatus;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.shared.vo.Quantity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductOutputPort productOutputPort;

    @InjectMocks
    private ProductUseCaseImpl productService;

    @DisplayName("특정상품 조회")
    @Test
    void getProduct() throws Exception {
        String productName = "mock 상품";
        Quantity quantity = Quantity.create(150);
        Money money = Money.create(5000);

        Product exProduct = Product.builder()
                .productId(ProductId.create())
                .productName(productName)
                .description("테스트용입니다")
                .price(money)
                .stockQuantity(quantity)
                .status(ProductStatus.fromStockQuantity(quantity))
                .build();

        // given : mock 동작 정의
        when(productOutputPort.findById(any()))
                .thenReturn(Optional.of(exProduct));

        // when

        ProductDetail dto = productService.getProduct(exProduct.getProductId());

        // then
        assertThat(dto.productId()).isEqualTo(exProduct.getProductId().id());
        assertThat(dto.name()).isEqualTo(exProduct.getProductName());

        verify(productOutputPort).findById(any());
    }
}
