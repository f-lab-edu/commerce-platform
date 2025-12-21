package com.commerce.platform.core.application.port.in;


import com.commerce.platform.core.application.port.in.dto.RegistryCardCommand;

public interface CustomerUseCase {
    void registryPayCard(RegistryCardCommand command);
}
