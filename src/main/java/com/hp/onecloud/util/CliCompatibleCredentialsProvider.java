package com.hp.onecloud.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;

public class CliCompatibleCredentialsProvider implements AWSCredentialsProvider {
    private final ProfileCredentialsProvider delegate;

    public CliCompatibleCredentialsProvider() {
        this.delegate = ProfileCredentialsProvider.create();
    }

    public CliCompatibleCredentialsProvider(String profileName) {
        this.delegate = ProfileCredentialsProvider.create("my-profile-name");
    }

    @Override
    public AWSCredentials getCredentials() {
        AwsCredentials credentials = delegate.resolveCredentials();

        if (credentials instanceof AwsSessionCredentials) {
            AwsSessionCredentials sessionCredentials = (AwsSessionCredentials) credentials;
            return new BasicSessionCredentials(sessionCredentials.accessKeyId(),
                    sessionCredentials.secretAccessKey(),
                    sessionCredentials.sessionToken());
        }

        return new BasicAWSCredentials(credentials.accessKeyId(), credentials.secretAccessKey());
    }

    @Override
    public void refresh() {
        throw new UnsupportedOperationException();
    }
}