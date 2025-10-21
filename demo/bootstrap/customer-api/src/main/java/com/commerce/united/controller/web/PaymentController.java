package com.commerce.united.controller.web;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/payment")
public class PaymentController {

    @PostMapping("/product/{productId}")
    public String doPayByProduct(@PathVariable String productId) {
        return null;
    }

    @PostMapping("/order/{orderId}")
    public String doPayByOrder(@PathVariable String orderId) {
        return null;
    }
}
