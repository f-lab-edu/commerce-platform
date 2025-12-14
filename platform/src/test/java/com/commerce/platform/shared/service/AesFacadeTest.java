package com.commerce.platform.shared.service;

import com.commerce.shared.service.AesCryptoFacade;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class AesFacadeTest {
    private AesCryptoFacade cryptoFacade;
    private String testKey;

    @BeforeEach
    void setUp() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        testKey = Base64.getEncoder().encodeToString(key);

        cryptoFacade = new AesCryptoFacade(testKey);
    }

    @Test
    void encrypt_decrypt() {
        String plainText = "테스트입니다.";

        // when
        String encrypted = cryptoFacade.encrypt(plainText);
        String decrypted = cryptoFacade.decrypt(encrypted);

        // then
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(decrypted).isEqualTo(plainText);
    }
}