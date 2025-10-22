package com.commerce.united.service;

import com.commerce.united.port.in.ProductUseCase;
import com.commerce.united.port.in.dto.ProductInfo;
import com.commerce.united.port.out.ProductOutputPort;
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
