package com.commerce.platform.core.domain.service;

import com.commerce.platform.bootstrap.dto.product.ProductInfo;
import com.commerce.platform.core.application.in.ProductUseCase;
import com.commerce.platform.core.application.out.ProductOutputPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService implements ProductUseCase {
    private final ProductOutputPort productOutputPort;

    @Override
    public List<ProductInfo> getProductList() {
        return productOutputPort.findAll().stream()
                .map(ProductInfo::from)
                .collect(Collectors.toList());
    }

    @Override
    public ProductInfo getProduct(String productId) throws Exception {
        return productOutputPort.findById(productId)
                .map(ProductInfo::from)
                .orElseThrow(() -> new Exception("해당 상품 없음"));

    }
}
