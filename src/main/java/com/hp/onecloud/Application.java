package com.hp.onecloud;

import com.hp.onecloud.config.AwsUtil;
import com.hp.onecloud.config.MysqlConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import static com.hp.onecloud.config.SecretConstants.*;
import static com.hp.onecloud.config.SecretConstants.MYSQL_PORT;

@SpringBootApplication
public class Application {

    private static final Logger log = LoggerFactory.getLogger(MysqlConfig.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner run() throws Exception {
        return (String[] args) -> {
//            User user1 = new User("John", "john@domain.com");
//            User user2 = new User("Julie", "julie@domain.com");
//            userRepository.save(user1);
//            userRepository.save(user2);
//            userRepository.findAll().forEach(user -> System.out.println(user);

            AwsUtil awsUtil = new AwsUtil();

            String mysqlHost = "metadata-api-dev.cluster-cfagqf2imh1x.us-west-2.rds.amazonaws.com";
            String dbname = "metadata";
            String username = "peterw";
            int mysqlPort = 3306;

            try {
                awsUtil.setSslProperties("rds-ca-bundle.pem");
                Connection connection = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort,
                        awsUtil.setMySqlConnectionProperties(mysqlHost, username));

                log.info("SSL Key File : {}", System.getProperty("javax.net.ssl.trustStore"));
                //verify the connection is successful
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT 'Success!' FROM DUAL;");
                while (rs.next()) {
                    String id = rs.getString(1);
                    log.info("mysql connection test id : {}", id);
                }

                //close the connection
                stmt.close();
                connection.close();
            }
            catch (Exception ex) {
                log.info("MySQL connection setup error -----> {}", ex.getMessage());
            }

        };
    }
}