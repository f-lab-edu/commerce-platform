package com.commerce.united.controller.web;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/customer")
public class CustomerController {

    @PostMapping
    public String signUp() {
        return null;
    }

    @PatchMapping("/{customerId}")
    public String withdraw(String customerId) {
        return null;
    }

    @PostMapping("/card")
    public String registerCard() {
        return null;
    }
}
