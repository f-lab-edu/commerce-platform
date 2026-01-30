package com.commerce.platform.bootstrap.admin;

import com.commerce.platform.bootstrap.dto.product.CreateProductRequest;
import com.commerce.platform.bootstrap.dto.product.UpdateStockRequest;
import com.commerce.platform.core.application.port.in.ProductUseCase;
import com.commerce.platform.core.application.port.in.dto.UpdateStockCommand;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.vo.Quantity;
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
        ProductId productId = productUseCase.createProduct(CreateProductRequest
                .to(productRequest));

        return ResponseEntity.ok("[성공] ProductId: " + productId.id());
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<String> updateStock(
            @PathVariable String productId,
            @Valid @RequestBody UpdateStockRequest stockRequest) {

        UpdateStockCommand stockCommand = new UpdateStockCommand(
                ProductId.of(productId),
                Quantity.create(stockRequest.quantity()),
                stockRequest.operation());

        Quantity nowQuantity = productUseCase.updateStock(stockCommand);

        return ResponseEntity.ok("[성공] 최종수량: " + nowQuantity.value());
    }
}
