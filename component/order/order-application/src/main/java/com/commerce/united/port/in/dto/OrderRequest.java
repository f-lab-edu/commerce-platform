package com.commerce.united.port.in.dto;

public record OrderRequest(
        String productId,
        int quantity
){
}
