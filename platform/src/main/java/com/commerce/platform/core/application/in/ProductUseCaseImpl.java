package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.out.ProductOutputPort;
import com.commerce.platform.core.application.vo.UpdateStockCommand;
import com.commerce.platform.core.domain.aggreate.Product;
import com.commerce.platform.core.domain.vo.ProductId;
import com.commerce.platform.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.commerce.platform.shared.exception.BusinessError.PRODUCT_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class ProductUseCaseImpl implements ProductUseCase {
    private final ProductOutputPort productOutputPort;

    @Override
    public List<Product> getProductList(int page) {
        return productOutputPort.findAll();
    }

    @Override
    public Product getProduct(ProductId productId) {
        return productOutputPort.findById(productId)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
    }

    @Override
    public ProductId createProduct(Product product) {
        productOutputPort.save(product);
        return product.getProductId();
    }

    @Override
    public Product updateStock(UpdateStockCommand stockCommand) {
        Product product = productOutputPort.findById(stockCommand.productId())
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));

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
