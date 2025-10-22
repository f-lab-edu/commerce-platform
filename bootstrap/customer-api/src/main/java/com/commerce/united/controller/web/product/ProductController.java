package com.commerce.united.controller.web.product;

import com.commerce.united.port.in.ProductUseCase;
import com.commerce.united.port.in.dto.ProductInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController("/product")
public class ProductController {
    private final ProductUseCase productUseCase;

    @GetMapping
    public List<ProductInfo> getProductList() {
        return productUseCase.getProductList();
    }

    @GetMapping("/{productId}")
    public ProductInfo getProduct(@PathVariable String productId) {
        try {
            return productUseCase.getProduct(productId);
        } catch (Exception e) {
            return null;
        }
    }
}
