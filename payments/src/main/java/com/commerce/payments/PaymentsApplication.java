package com.commerce.payments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Payments 마이크로서비스
 * gRPC 서버로 동작
 */
@SpringBootApplication(scanBasePackages = {
        "com.commerce.payments",
        "com.commerce.shared"
})
public class PaymentsApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(PaymentsApplication.class, args);
    }
}
