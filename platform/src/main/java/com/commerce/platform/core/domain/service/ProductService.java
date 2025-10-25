package com.commerce.platform.core.domain.service;

import com.commerce.platform.bootstrap.dto.product.ProductInfo;
import com.commerce.platform.core.application.in.ProductUseCase;
import com.commerce.platform.core.application.out.ProductOutputPort;
import com.commerce.platform.core.application.vo.UpdateStockCommand;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.vo.ProductId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ProductService implements ProductUseCase {
    private final ProductOutputPort productOutputPort;

    @Override
    public List<ProductInfo> getProductList(int page) {
        return productOutputPort.findAll().stream()
                .map(ProductInfo::from)
                .collect(Collectors.toList());
    }

    @Override
    public ProductInfo getProduct(ProductId productId) throws Exception {
        return productOutputPort.findById(productId)
                .map(ProductInfo::from)
                .orElseThrow(() -> new Exception("해당 상품 없음"));

    }

    @Override
    public void createProduct(Product product) {
        productOutputPort.save(product);
    }

    @Override
    public Product updateStock(UpdateStockCommand stockCommand) throws Exception {
        Product product = productOutputPort.findById(stockCommand.productId())
                .orElseThrow(() -> new Exception("해당 상품 없음"));

        Product updatedProduct = switch (stockCommand.stockOperation()) {
            case SET      -> product.changeStockQuantity(stockCommand.quantity());
            case INCREASE -> product.increaseStock(stockCommand.quantity());
            case DECREASE -> product.decreaseStock(stockCommand.quantity());
            default -> throw new IllegalStateException("Unexpected value: " + stockCommand.stockOperation());
        };

        productOutputPort.save(updatedProduct);

        return updatedProduct;
    }
}
