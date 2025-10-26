package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.bootstrap.dto.product.ProductDetail;
import com.commerce.platform.bootstrap.dto.product.ProductInfo;
import com.commerce.platform.core.application.in.ProductUseCase;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.vo.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/products")
@RestController
public class ProductController {
    private final ProductUseCase productUseCase;

    @GetMapping
    public ResponseEntity<List<ProductInfo>> getProducts(@RequestParam(required = false, defaultValue = "0") int page) {
        List<ProductInfo> productList = productUseCase.getProductList(page)
                .stream()
                .map(ProductInfo::from)
                .toList();

        return ResponseEntity.ok(productList);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetail> getProduct(@PathVariable String productId) {
        Product product = productUseCase.getProduct(ProductId.of(productId));
        ProductDetail pd = ProductDetail.from(product);

        return ResponseEntity.ok(pd);
    }
}
