package com.hp.onecloud.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Properties for MySQL connections
 */
@Getter
@Setter
@NoArgsConstructor
@Configuration("mysqlProperties")
@ConfigurationProperties(prefix = "mysql")
public class MysqlProperties {

    private String driver;
    private String url;
    private String username;
    private String password;

}
