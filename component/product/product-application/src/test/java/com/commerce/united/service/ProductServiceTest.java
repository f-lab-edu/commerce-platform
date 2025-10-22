package com.commerce.united.service;

import com.commerce.united.port.in.dto.ProductInfo;
import com.commerce.united.port.out.ProductOutputPort;
import domain.aggregate.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductOutputPort productOutputPort;

    @InjectMocks
    private ProductService productService;

    @Test
    void getProductList() {
        List<Product> list = new ArrayList<>();
        String prefix = "mock_상품_";
        long price = 1000;

        for (int i = 0; i < 5; i++) {
            list.add(Product.create(prefix + i, price + i));
        }

        // given : mock 동작 정의
        when(productOutputPort.findAll())
                .thenReturn(list);

        // when
        List<ProductInfo> productList = productService.getProductList();

        // then
        assertThat(productList.size()).isEqualTo(list.size());
    }

    @DisplayName("특정상품 조회")
    @Test
    void getProduct() throws Exception {
        String productName = "mock 상품";
        long price = 1004;

        Product product = Product.create(productName, price);
        String productId = product.getProductId();

        // given : mock 동작 정의
        when(productOutputPort.findById(productId))
                .thenReturn(Optional.of(product));

        // when
        ProductInfo productInfo = productService.getProduct(productId);

        // then
        assertThat(productId).isEqualTo(productInfo.productId());
        assertThat(productName).isEqualTo(productInfo.name());

        // port 호출 검증
        verify(productOutputPort).findById(productId);
    }
}