package com.hp.onecloud.config;

import com.amazonaws.services.rds.auth.RdsIamAuthTokenGenerator;
import com.amazonaws.services.rds.auth.GetIamAuthTokenRequest;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import java.net.URL;

@Component
public class AwsUtil {
    private static final DefaultAWSCredentialsProviderChain creds = new DefaultAWSCredentialsProviderChain();
    private static final String AWS_ACCESS_KEY = creds.getCredentials().getAWSAccessKeyId();
    private static final String AWS_SECRET_KEY = creds.getCredentials().getAWSSecretKey();

    private static final int RDS_INSTANCE_PORT = 3306;
    private static final String REGION_NAME = "us-west-2";
    //private static final String DB_USER = "jane_doe";
    //private static final String JDBC_URL = "jdbc:mysql://" + RDS_INSTANCE_HOSTNAME + ":" + RDS_INSTANCE_PORT;

    //private static final String SSL_CERTIFICATE = "rds-ca-2019-us-west-2.pem";

    private static final String KEY_STORE_TYPE = "JKS";
    private static final String KEY_STORE_PROVIDER = "SUN";
    private static final String KEY_STORE_FILE_PREFIX = "sys-connect-via-ssl-test-cacerts";
    private static final String KEY_STORE_FILE_SUFFIX = ".jks";
    private static final String DEFAULT_KEY_STORE_PASSWORD = "changeit";

//    public static void main(String[] args) throws Exception {
//        //get the connection
//        Connection connection = getDBConnectionUsingIam();
//
//        //verify the connection is successful
//        Statement stmt= connection.createStatement();
//        ResultSet rs=stmt.executeQuery("SELECT 'Success!' FROM DUAL;");
//        while (rs.next()) {
//            String id = rs.getString(1);
//            System.out.println(id); //Should print "Success!"
//        }
//
//        //close the connection
//        stmt.close();
//        connection.close();
//
//        clearSslProperties();
//
//    }

//    /**
//     * This method returns a connection to the db instance authenticated using IAM Database Authentication
//     * @return
//     * @throws Exception
//     */
//    private static Connection getDBConnectionUsingIam() throws Exception {
//        setSslProperties();
//        return DriverManager.getConnection(JDBC_URL, setMySqlConnectionProperties());
//    }

    /**
     * This method sets the mysql connection properties which includes the IAM Database Authentication token
     * as the password. It also specifies that SSL verification is required.
     * @return
     */
    public Properties setMySqlConnectionProperties(String hostName, String dbUser) {
        Properties mysqlConnectionProperties = new Properties();
        mysqlConnectionProperties.setProperty("verifyServerCertificate","true");
        mysqlConnectionProperties.setProperty("useSSL", "true");
        mysqlConnectionProperties.setProperty("user",dbUser);
        mysqlConnectionProperties.setProperty("password",generateAuthToken(hostName, RDS_INSTANCE_PORT, dbUser));

        return mysqlConnectionProperties;
    }

    /**
     * This method generates the IAM Auth Token.
     * An example IAM Auth Token would look like follows:
     * btusi123.cmz7kenwo2ye.rds.cn-north-1.amazonaws.com.cn:3306/?Action=connect&DBUser=iamtestuser&X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Date=20171003T010726Z&X-Amz-SignedHeaders=host&X-Amz-Expires=899&X-Amz-Credential=AKIAPFXHGVDI5RNFO4AQ%2F20171003%2Fcn-north-1%2Frds-db%2Faws4_request&X-Amz-Signature=f9f45ef96c1f770cdad11a53e33ffa4c3730bc03fdee820cfdf1322eed15483b
     * @return
     */
    private String generateAuthToken(String hostName, int port, String dbUser) {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(AWS_ACCESS_KEY, AWS_SECRET_KEY);

        RdsIamAuthTokenGenerator generator = RdsIamAuthTokenGenerator.builder()
                .credentials(new AWSStaticCredentialsProvider(awsCredentials)).region(REGION_NAME).build();
        return generator.getAuthToken(GetIamAuthTokenRequest.builder()
                .hostname(hostName).port(port).userName(dbUser).build());
    }

    /**
     * This method sets the SSL properties which specify the key store file, its type and password:
     * @throws Exception
     */
    public void setSslProperties(String sslPemFile) throws Exception {
        System.setProperty("javax.net.ssl.trustStore", createKeyStoreFile(sslPemFile));
        System.setProperty("javax.net.ssl.trustStoreType", KEY_STORE_TYPE);
        System.setProperty("javax.net.ssl.trustStorePassword", DEFAULT_KEY_STORE_PASSWORD);
    }

    /**
     * This method returns the path of the Key Store File needed for the SSL verification during the IAM Database Authentication to
     * the db instance.
     * @return
     * @throws Exception
     */
    private String createKeyStoreFile(String sslPemFile) throws Exception {
        return createKeyStoreFile(createCertificate(sslPemFile)).getPath();
    }

    /**
     *  This method generates the SSL certificate
     * @return
     * @throws Exception
     */
    private X509Certificate createCertificate(String sslPemFile) throws Exception {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        try (InputStream certInputStream = getClass().getClassLoader().getResourceAsStream(sslPemFile)) {
            return (X509Certificate) certFactory.generateCertificate(certInputStream);
        }
    }

    /**
     * This method creates the Key Store File
     * @param rootX509Certificate - the SSL certificate to be stored in the KeyStore
     * @return
     * @throws Exception
     */
    private File createKeyStoreFile(X509Certificate rootX509Certificate) throws Exception {
        File keyStoreFile = File.createTempFile(KEY_STORE_FILE_PREFIX, KEY_STORE_FILE_SUFFIX);
        try (FileOutputStream fos = new FileOutputStream(keyStoreFile.getPath())) {
            KeyStore ks = KeyStore.getInstance(KEY_STORE_TYPE, KEY_STORE_PROVIDER);
            ks.load(null);
            ks.setCertificateEntry("rootCaCertificate", rootX509Certificate);
            ks.store(fos, DEFAULT_KEY_STORE_PASSWORD.toCharArray());
        }
        return keyStoreFile;
    }

    /**
     * This method clears the SSL properties.
     * @throws Exception
     */
    private void clearSslProperties() throws Exception {
        System.clearProperty("javax.net.ssl.trustStore");
        System.clearProperty("javax.net.ssl.trustStoreType");
        System.clearProperty("javax.net.ssl.trustStorePassword");
    }

}
