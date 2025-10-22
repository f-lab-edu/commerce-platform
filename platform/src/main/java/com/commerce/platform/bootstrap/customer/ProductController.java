package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.core.application.in.ProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/product")
@RestController
public class ProductController {
    private final ProductUseCase productUseCase;
}
