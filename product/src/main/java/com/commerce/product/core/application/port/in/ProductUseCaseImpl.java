package com.commerce.product.core.application.port.in;

import com.commerce.product.core.application.port.in.dto.ProductDetail;
import com.commerce.product.core.application.port.out.ProductOutputPort;
import com.commerce.product.core.domain.aggregate.Product;
import com.commerce.shared.vo.ProductId;
import com.commerce.shared.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.commerce.shared.exception.BusinessError.PRODUCT_NOT_FOUND;

@RequiredArgsConstructor
@Service
public class ProductUseCaseImpl implements ProductUseCase {
    private final ProductOutputPort productOutputPort;

    @Override
    public List<Product> getProductList(int page) {
        return productOutputPort.findAll();
    }

    @Override
    public ProductDetail getProduct(ProductId productId) {
        return productOutputPort.findById(productId)
                .map(ProductDetail::from)
                .orElseThrow(() -> new BusinessException(PRODUCT_NOT_FOUND));
    }

    @Override
    public ProductId createProduct(Product product) {
        productOutputPort.save(product);
        return product.getProductId();
    }
}
