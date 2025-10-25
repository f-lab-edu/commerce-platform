package com.commerce.platform.bootstrap.admin;

import com.commerce.platform.bootstrap.dto.product.CreateProductRequest;
import com.commerce.platform.bootstrap.dto.product.UpdateStockRequest;
import com.commerce.platform.core.application.in.ProductUseCase;
import com.commerce.platform.core.application.vo.UpdateStockCommand;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.platform.core.domain.vo.Quantity;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/admin/products")
@RestController
public class ProductAdController {
    private final ProductUseCase productUseCase;

    @PostMapping
    public ResponseEntity<String> createProduct(@Valid @RequestBody CreateProductRequest productRequest) {
        Product product = Product.from(productRequest);
        productUseCase.createProduct(product);

        return ResponseEntity.ok("[성공] ProductId: " + product.getProductId());
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<String> updateStock(
            @PathVariable String productId,
            @Valid @RequestBody UpdateStockRequest stockRequest) throws Exception {

        UpdateStockCommand stockCommand = new UpdateStockCommand(
                ProductId.of(productId),
                Quantity.create(stockRequest.quantity()),
                stockRequest.operation());

        Product updatedProduct = productUseCase.updateStock(stockCommand);

        return ResponseEntity.ok("[성공] 최종수량: " + updatedProduct.getStockQuantity().getValue());
    }
}
