package com.hp.onecloud.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static com.hp.onecloud.config.SecretConstants.CSI_MOUNT_PATH;

public class SecretStorage implements InitializingBean {

    private static Logger log = LoggerFactory.getLogger(SecretStorage.class);

    @Autowired
    private ConfigurableEnvironment env;

    @Override
    public void afterPropertiesSet()  {

        // Load all AWS secret into environment variables
        try (Stream<Path> paths = Files.walk(Path.of(CSI_MOUNT_PATH))) {
            paths.filter(Files::isRegularFile)
                    .forEach(f -> {
                        try {
                            String secretKey = f.getFileName().toString();
                            String secretValue = Files.readAllLines(f).get(0);

                            log.info("Onecloud Secret Management -> " + secretKey + " : " + secretValue);

                            System.setProperty(secretKey, secretValue);

                        } catch (IOException e) {
                            log.error(CSI_MOUNT_PATH + " secret properties doesn't exist " + e.getMessage());
                        }
                    });
        }
        catch (IOException e) {
            log.error(CSI_MOUNT_PATH + " properties doesn't exist");
        }
    }

}
