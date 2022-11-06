package com.hp.onecloud;

import com.hp.onecloud.config.AwsUtil;
import com.hp.onecloud.config.MysqlConfig;
import com.hp.onecloud.util.Util;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import software.amazon.jdbc.PropertyDefinition;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

import static com.hp.onecloud.config.SecretConstants.*;
import static com.hp.onecloud.config.SecretConstants.MYSQL_PORT;


@SpringBootApplication
public class Application {

    private static final Logger log = LoggerFactory.getLogger(MysqlConfig.class);

    public static final String MYSQL_CONNECTION_STRING =
            "jdbc:aws-wrapper:mysql://metadata-api-dev.cluster-cfagqf2imh1x.us-west-2.rds.amazonaws.com:3306";
    private static final String USERNAME = "peterw";

    @Autowired
    MysqlConfig mysqlConfig;
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

            final Properties properties = new Properties();

            // Enable AWS IAM database authentication and configure driver property values
            properties.setProperty(PropertyDefinition.PLUGINS.name, "iam");
            properties.setProperty(PropertyDefinition.USER.name, USERNAME);

            // Attempt a connection
            try (Connection conn = DriverManager.getConnection(MYSQL_CONNECTION_STRING, properties);
                 Statement statement = conn.createStatement();
                 ResultSet result = statement.executeQuery("select user from mysql.user")) {
                    log.info("Test Result ==================>" + Util.getResult(result));
                 }



           // AwsUtil awsUtil = new AwsUtil();

            String mysqlHost = "metadata-api-dev.cluster-cfagqf2imh1x.us-west-2.rds.amazonaws.com";
            String dbname = "metadata";
            String username = "peterw";
            int mysqlPort = 3306;

//            try {
//                awsUtil.setSslProperties("rds-ca-bundle.pem");
//                String jdbcURL = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort;
//                Connection connection = DriverManager.getConnection(jdbcURL,
//                        awsUtil.setMySqlConnectionProperties(mysqlHost, username));
//
//                log.info("SSL Key File : {}", System.getProperty("javax.net.ssl.trustStore"));
//                //verify the connection is successful
//                Statement stmt = connection.createStatement();
//                ResultSet rs = stmt.executeQuery("SELECT 'Success!' FROM DUAL;");
//                while (rs.next()) {
//                    String id = rs.getString(1);
//                    log.info("mysql connection test id : {}", id);
//                }
//
//                //close the connection
//                stmt.close();
//                connection.close();
//            }
//            catch (Exception ex) {
//                log.info("MySQL connection setup error -----> {}", ex.getMessage());
//            }

            mysqlConfig.dataSource();
        };
    }
}