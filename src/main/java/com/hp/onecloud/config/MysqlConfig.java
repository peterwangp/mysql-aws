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
import software.amazon.jdbc.ds.AwsWrapperDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

import static com.hp.onecloud.config.SecretConstants.*;

@Configuration
//@Conditional(MysqlLoadCondition.class)
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
    public MysqlConfig(Environment environment, MysqlProperties mysqlProperties, @Qualifier("secretStorage") SecretStorage secretStorage) {
        this.environment = environment;
        this.mysqlProperties = mysqlProperties;
        this.secretStorage = secretStorage;
    }

    @DependsOn("secretStorage")
    @Bean(name = "mySqlDataSource")
    @Primary
    public HikariDataSource dataSource()  {

        try {

            //DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create().type(HikariDataSource.class);

//            String[] profiles = this.environment.getActiveProfiles();
//
//            boolean contains = Arrays.stream(profiles).anyMatch(LOCAL::equals);
//
//            log.info("profiles -> {} ", profiles[0]);
            boolean contains = false;

            if (!contains) {

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

//                try {
//                    awsUtil.setSslProperties(AWS_CERTS_FILE);
//                    Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
//
//
//                    Connection connection = DriverManager.getConnection("jdbc:mysql://" + mysqlHost + ":" + mysqlPort,
//                            awsUtil.setMySqlConnectionProperties(mysqlHost, username));
//
//                    log.info("SSL Key File : {}", System.getProperty("javax.net.ssl.trustStore"));
//                    //verify the connection is successful
//                    Statement stmt = connection.createStatement();
//                    ResultSet rs = stmt.executeQuery("SELECT 'Success!' FROM DUAL;");
//                    while (rs.next()) {
//                        String id = rs.getString(1);
//                        log.info("mysql connection test id : {}", id);
//                    }
//
//                    //close the connection
//                    stmt.close();
//                    connection.close();
//                }
//                catch (Exception ex) {
//                    log.info("MySQL connection setup error {}", ex.getMessage());
//                }


                String mysqlUrl = "jdbc:mysql://" + mysqlHost + ":" + mysqlPort + "/" + dbname + MYSQL_EXTRA_PARM;

                log.info("MySQL ARN connection info -> username : {} password : {} url : {} ", username, password, mysqlUrl);

                String token = this.getToken(mysqlHost, Integer.parseInt(mysqlPort), username);

                log.info("mysql info -> token : {} ", token);

                HikariDataSource ds = new HikariDataSource();

                // Configure the connection pool:
                ds.setUsername("peterw");
                ds.setPassword(token);

                // Specify the underlying datasource for HikariCP:
                ds.setDataSourceClassName(AwsWrapperDataSource.class.getName());

                // Configure AwsWrapperDataSource:
                ds.addDataSourceProperty("jdbcProtocol", "jdbc:postgresql:");
                ds.addDataSourceProperty("databasePropertyName", "databaseName");
                ds.addDataSourceProperty("portPropertyName", "portNumber");
                ds.addDataSourceProperty("serverPropertyName", "serverName");

                // Specify the driver-specific data source for AwsWrapperDataSource:
                ds.addDataSourceProperty("targetDataSourceClassName", "org.postgresql.ds.PGSimpleDataSource");

                // Configuring PGSimpleDataSource:
                Properties targetDataSourceProps = new Properties();
                targetDataSourceProps.setProperty("serverName", mysqlHost);
                targetDataSourceProps.setProperty("databaseName", dbname);
                targetDataSourceProps.setProperty("portNumber", mysqlPort);

                ds.addDataSourceProperty("targetDataSourceProperties", targetDataSourceProps);

                log.info("MySQL ARN connection string : {} ", mysqlUrl);

                // Attempt a connection:
                try (final Connection conn = ds.getConnection();
                     final Statement statement = conn.createStatement();
                     final ResultSet rs = statement.executeQuery("SELECT * from aurora_db_instance_identifier()")) {
                    while (rs.next()) {
                        System.out.println(rs.getString(1));
                    }
                }

                return ds;

            } else {
                DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create().type(HikariDataSource.class);

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

                HikariDataSource ds = (HikariDataSource) dataSourceBuilder.build();

                return ds;

            }

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
