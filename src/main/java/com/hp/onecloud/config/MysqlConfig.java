package com.hp.onecloud.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.rds.auth.GetIamAuthTokenRequest;
import com.amazonaws.services.rds.auth.RdsIamAuthTokenGenerator;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import static com.hp.onecloud.config.SecretConstants.*;

@Configuration
@Conditional(MysqlLoadCondition.class)
public class MysqlConfig extends HikariDataSource {

    private static final Logger log = LoggerFactory.getLogger(MysqlConfig.class);

    private static final String AWS_CERTS_FILE = "rds-ca-bundle.pem";
    private static final String LOCAL = "local";
    private static final String MYSQL_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private static final String MYSQL_EXTRA_PARM = "?useSSL=true&requireSSL=true&verifyServerCertificate=true&clientCertificateKeyStoreUrl=file:/usr/app/truststore.pkcs12&clientCertificateKeyStorePassword=changeit&clientCertificateKeyStoreType=PKCS12&trustCertificateKeyStoreUrl=file:/usr/app/truststore.pkcs12&trustCertificateKeyStoreType=PKCS12&trustCertificateKeyStorePassword=changeit&useAwsIam=true";
    //private static final String MYSQL_EXTRA_PARM = "?verifyServerCertificate=true&useSSL=true&requireSSL=true";
    //private static final String MYSQL_EXTRA_PARM = "?useConfigs=maxPerformance&characterEncoding=utf8&sslMode=VERIFY_CA&trustCertificateKeyStoreType=PKCS12&trustCertificateKeyStoreUrl=file:///usr/app/truststore.pkcs12&trustCertificateKeyStorePassword=changeit";

    //private static final String MYSQL_EXTRA_PARM = "?useConfigs=maxPerformance&characterEncoding=utf8&useSSL=true&verifyServerCertificate=true&trustCertificateKeyStoreType=PKCS12&trustCertificateKeyStoreUrl=file:///usr/app/truststore.pkcs12&trustCertificateKeyStorePassword=changeit&useAwsIam=true";
    //private static final String MYSQL_EXTRA_PARM = "?useSSL=false&server&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true&useAwsIam=true";
    private Environment environment;
    private MysqlProperties mysqlProperties;
    private SecretStorage secretStorage;

    private String mysqlHost;  //jdbc:mysql://localhost:3306/testdb
    private String mysqlPort;
    private String dbname;
    private String username;
    private String password;

    @Autowired
    MysqlConfig(Environment environment, MysqlProperties mysqlProperties, @Qualifier("secretStorage") SecretStorage secretStorage) {
        this.environment = environment;
        this.mysqlProperties = mysqlProperties;
        this.secretStorage = secretStorage;
    }

    @DependsOn("secretStorage")
    @Bean(name = "mySqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    @Primary
    public HikariDataSource dataSource()  {

        try {

            AwsUtil awsUtil = new AwsUtil();

            DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create().type(HikariDataSource.class);

            String[] profiles = this.environment.getActiveProfiles();

            boolean contains = Arrays.stream(profiles).anyMatch(LOCAL::equals);

            log.info("profiles -> {} ", profiles[0]);

            if (!contains) {



//            System.setProperty("javax.net.ssl.trustStore", "/usr/app/truststore.jks");
//            System.setProperty("javax.net.ssl.trustStoreType", "JKS");
//            System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

                mysqlHost = System.getProperty(MYSQL_HOST);
                dbname = System.getProperty(MYSQL_DBNAME);
                //username = System.getProperty(MYSQL_USERNAME);
                username = "peterw";
                password = System.getProperty(MYSQL_PASSWORD);
                mysqlPort = System.getProperty(MYSQL_PORT);

                //TODO remove this logic, once we have port setup in secret manager
                if (mysqlPort == null || mysqlPort.length() == 0) {
                    mysqlPort = "3306";
                }

                try {
                    awsUtil.setSslProperties(AWS_CERTS_FILE);
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
                    log.info("MySQL connection setup error {}", ex.getMessage());
                }


                String mysqlUrl = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + dbname + MYSQL_EXTRA_PARM;

                log.info("MySQL ARN connection info -> username : {} password : {} url : {} ", username, password, mysqlUrl);

                String token = this.getToken(mysqlHost, Integer.parseInt(mysqlPort), username);

                log.info("mysql info -> token : {} ", token);

                dataSourceBuilder.url(mysqlUrl);
                dataSourceBuilder.username(username);
                dataSourceBuilder.password(token);
                dataSourceBuilder.driverClassName(MYSQL_DRIVER_CLASS);

                log.info("MySQL ARN connection string : {} ", mysqlUrl);

            } else {
                mysqlHost = environment.getProperty("mysql.host");
                mysqlPort = environment.getProperty("mysql.port");
                username = environment.getProperty("mysql.username");
                password = environment.getProperty("mysql.password");
                dbname = environment.getProperty("mysql.dbname");

                String mysqlUrl = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + dbname + "?useSSL=false&server&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true&useAwsIam=true";
                dataSourceBuilder.url(mysqlUrl);
                dataSourceBuilder.username(username);
                dataSourceBuilder.password(password);
                dataSourceBuilder.driverClassName(MYSQL_DRIVER_CLASS);

                log.info("MySQL local connection string : {}  username : {} password : {} ", mysqlUrl, username, password);

            }

            HikariDataSource ds = (HikariDataSource) dataSourceBuilder.build();

            return ds;
        }
        catch (Exception e) {
            log.info("MySQL local connection string failed with error message : {} ", e.getMessage());
            return null;
        }
    }


    public String getToken(String hostName, int port, String userName) {

        RdsIamAuthTokenGenerator authTokenGenerator = RdsIamAuthTokenGenerator.builder()
                .credentials(new DefaultAWSCredentialsProviderChain())
                .region(String.valueOf(new DefaultAwsRegionProviderChain().getRegion()))
                .build();

        GetIamAuthTokenRequest iamAuthTokenRequest = GetIamAuthTokenRequest.builder()
                .hostname(hostName)
                .port(port)
                .userName(userName)
                .build();

        return authTokenGenerator.getAuthToken(iamAuthTokenRequest);
    }


}