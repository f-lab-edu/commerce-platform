package com.commerce.platform.core.application.in;

import com.commerce.platform.core.application.in.dto.RegistryCardCommand;

public interface CustomerUseCase {
    void registryPayCard(RegistryCardCommand command);
}
