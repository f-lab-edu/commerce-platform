package com.commerce.platform.bootstrap.dto.order;

public record OrderRequest(
        String productId,
        int quantity
){
}
