package com.commerce.platform.bootstrap.customer;

import com.commerce.platform.bootstrap.dto.customer.RegistryCardRequest;
import com.commerce.platform.core.application.in.CustomerUseCase;
import com.commerce.platform.core.application.in.dto.RegistryCardCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/customers")
@RestController
public class CustomerController {

    private final CustomerUseCase customerUsecase;

    @PostMapping("/card")
    public ResponseEntity<String> registryCard(@Valid @RequestBody RegistryCardRequest cardRequest) {
        RegistryCardCommand command = cardRequest.to();
        customerUsecase.registryPayCard(command);

        return ResponseEntity.ok("성공");
    }
}
