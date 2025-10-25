package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.bootstrap.dto.product.ProductInfo;
import com.commerce.platform.core.application.in.ProductUseCase;
import com.commerce.platform.core.domain.vo.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/products")
@RestController
public class ProductController {
    private final ProductUseCase productUseCase;

    @GetMapping
    public List<ProductInfo> getProducts(@RequestParam(required = false, defaultValue = "0") int page) {
        return productUseCase.getProductList(page);
    }

    @GetMapping("/{productId}")
    public ProductInfo getProduct(@PathVariable String productId) {
        try {
            return productUseCase.getProduct(ProductId.of(productId));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
