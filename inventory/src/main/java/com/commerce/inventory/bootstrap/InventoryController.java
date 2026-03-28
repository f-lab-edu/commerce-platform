package com.commerce.inventory.bootstrap;

import com.commerce.inventory.bootstrap.dto.UpdateStockRequest;
import com.commerce.inventory.core.application.port.in.InventoryUseCase;
import com.commerce.inventory.core.domain.aggregate.Inventory;
import com.commerce.shared.vo.ProductId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/inventory")
@RestController
public class InventoryController {

    private final InventoryUseCase inventoryUseCase;

    @GetMapping("/{productId}")
    public ResponseEntity<String> getStock(@PathVariable String productId) {
        Inventory inventory = inventoryUseCase.getStock(ProductId.of(productId));
        return ResponseEntity.ok("재고 수량: " + inventory.getQuantity().value());
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<String> updateStock(
            @PathVariable String productId,
            @Valid @RequestBody UpdateStockRequest stockRequest) {

        ProductId pid = ProductId.of(productId);

        switch (stockRequest.operation()) {
            case SET      -> inventoryUseCase.setStock(pid, stockRequest.quantity());
            case INCREASE -> inventoryUseCase.increaseStock(pid, stockRequest.quantity());
            case DECREASE -> inventoryUseCase.decreaseStock(pid, stockRequest.quantity());
        }
        return ResponseEntity.ok("성공");
    }
}