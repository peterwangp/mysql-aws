package com.hp.onecloud.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

@ExtendWith(MockitoExtension.class)
class SecretStorageTest {

    @Mock
    private ConfigurableEnvironment mockEnv;

    @Test
    void testAfterPropertiesSet() throws Exception {

        final Path resourcePath = Paths.get("src","test", "java","resources");
        final String absolutePath = resourcePath.toFile().getAbsolutePath();

        final Path newPath = Path.of(absolutePath);

        SecretStorage secretStorageUnderTest = new SecretStorage();

        MockedStatic<Path> staticPath = Mockito.mockStatic(Path.class);
        staticPath.when(() -> Path.of(Mockito.anyString()))
                    .thenReturn(newPath);

        secretStorageUnderTest.afterPropertiesSet();

    }

    @Test
    void testAfterPropertiesSet_Exception() throws Exception {


        SecretStorage secretStorageUnderTest = new SecretStorage();

        secretStorageUnderTest.afterPropertiesSet();

    }

    @Test
    void testAfterPropertiesSet_SetSecret() throws Exception {


        SecretStorage secretStorageUnderTest = new SecretStorage();


        try {
            final Path resourcePath = Paths.get("src","test", "java","resources");
            final String absolutePath = resourcePath.toFile().getAbsolutePath();

            ReflectionTestUtils.invokeMethod(secretStorageUnderTest, "setSecret", absolutePath + "/MONGOURI");

        }
        catch(Exception ex) {

        }

    }



}
