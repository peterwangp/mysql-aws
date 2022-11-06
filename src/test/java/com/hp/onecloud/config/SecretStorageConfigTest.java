package com.hp.onecloud.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SecretStorageConfigTest {

    private SecretStorageConfig secretStorageConfigUnderTest;

    @BeforeEach
    void setUp() {
        secretStorageConfigUnderTest = new SecretStorageConfig();
    }

    @Test
    void testGetSecretStorge() {

        final SecretStorage result = secretStorageConfigUnderTest.getSecretStorge();

    }
}
